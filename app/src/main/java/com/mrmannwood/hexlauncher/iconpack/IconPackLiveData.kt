package com.mrmannwood.hexlauncher.iconpack

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.executors.PackageManagerExecutor
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import com.mrmannwood.hexlauncher.Result
import com.mrmannwood.hexlauncher.executors.cpuBoundTaskExecutor
import java.util.concurrent.atomic.AtomicInteger

class IconPackLiveData(context: Context, private val packageName: String): LiveData<Result<List<Drawable>>>() {

    private val appContext = context.applicationContext
    private val isActive = AtomicBoolean(false)

    override fun onActive() {
        super.onActive()
        isActive.set(true)
        postValue(Result.loading())
        getAppFilterParser(
            { parser ->
                if(!isActive.get()) return@getAppFilterParser
                readAppFilter(
                    parser = parser,
                    onParseSuccess = { iconMap ->
                        if(!isActive.get()) return@readAppFilter
                        loadIcons(iconMap) {
                            postValue(Result.success(it))
                        }
                    },
                    onParseFailure = { exception -> postValue(Result.failure(exception)) }
                )
            },
            { postValue(Result.failure(AppFilterDoesNotExistException(packageName))) }
        )
    }

    override fun onInactive() {
        isActive.set(false)
        super.onInactive()
    }

    private fun getAppFilterParser(onAppFilter: (XmlPullParser) -> Unit, onNoAppFilter: () -> Unit) {
        PackageManagerExecutor.execute {
            val parser: XmlPullParser?

            val iconPackResources =
                appContext.packageManager.getResourcesForApplication(packageName)

            val id = iconPackResources.getIdentifier("appfilter", "xml", packageName)
            if (id > 0) {
                parser = iconPackResources.getXml(id)
            } else {
                parser = try {
                    XmlPullParserFactory.newInstance()
                        .apply { isNamespaceAware = true }
                        .newPullParser()
                        .apply { setInput(iconPackResources.assets.open("appfilter.xml"), "utf-8") }
                } catch (e: IOException) {
                    null
                }
            }

            if (parser != null) {
                onAppFilter(parser)
            } else {
                onNoAppFilter()
            }
        }
    }

    private fun readAppFilter(
        parser: XmlPullParser,
        onParseSuccess: (Map<String, String>) -> Unit,
        onParseFailure: (AppFilterParserException) -> Unit
    ) {
        cpuBoundTaskExecutor.execute {
            val icons = mutableMapOf<String, String>()
            var success = true
            try {
                while (true) {
                    when (parser.eventType) {
                        XmlPullParser.END_DOCUMENT -> break
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "iconback" -> Unit //TODO
                                "iconmask" -> Unit //TODO
                                "iconupon" -> Unit //TODO
                                "scale" -> Unit //TODO
                                "item" -> {
                                    var componentName: String? = null
                                    var drawableName: String? = null
                                    for (i in 0 until parser.attributeCount) {
                                        when (parser.getAttributeName(i)) {
                                            "component" -> componentName =
                                                parser.getAttributeValue(i)
                                            "drawable" -> drawableName = parser.getAttributeValue(i)
                                        }
                                    }
                                    if (componentName != null && drawableName != null) {
                                        icons[componentName] = drawableName
                                    }
                                }
                            }
                        }
                    }
                    parser.next()
                }
            } catch (e: Exception) {
                success = false
                onParseFailure(AppFilterParserException(packageName, e))
            }
            if (success) {
                onParseSuccess(icons)
            }
        }
    }

    private fun loadIcons(iconMap: Map<String, String>, onIconsLoaded: (List<Drawable>) -> Unit) {
        val drawables = Array<Drawable?>(iconMap.size) { null }
        val finished = AtomicInteger(0)
        iconMap.entries.forEachIndexed { idx, (componentName, drawableName) ->
            val pacman = appContext.packageManager.getResourcesForApplication(packageName)
            val id = pacman.getIdentifier(drawableName, "drawable", packageName)
            if (id > 0) {
                drawables[idx] = pacman.getDrawable(id)
            }
            if (finished.incrementAndGet() == iconMap.size - 1) {
                onIconsLoaded(drawables.mapNotNull { it }.toList())
            }
        }
    }

    private class AppFilterDoesNotExistException(packageName: String): Exception("Could not find AppFilter for $packageName")
    private class AppFilterParserException(packageName: String, e: Exception): Exception("Could not parse AppFilter for $packageName", e)
}