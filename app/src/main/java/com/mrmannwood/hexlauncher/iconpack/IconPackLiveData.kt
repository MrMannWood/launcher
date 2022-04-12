package com.mrmannwood.hexlauncher.iconpack

import android.content.ComponentName
import android.content.Context
import android.content.pm.ComponentInfo
import android.graphics.drawable.Drawable
import android.util.SparseArray
import androidx.core.content.res.ResourcesCompat
import androidx.core.util.forEach
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.executors.PackageManagerExecutor
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import com.mrmannwood.hexlauncher.Result
import com.mrmannwood.hexlauncher.executors.cpuBoundTaskExecutor
import com.mrmannwood.hexlauncher.launcher.Provider

class IconPackLiveData(context: Context, private val packageName: String): LiveData<Result<List<IconPackIconInfo>>>() {

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
                    onParseSuccess = { icons ->
                        postValue(Result.success(icons))
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
        onParseSuccess: (List<IconPackIconInfo>) -> Unit,
        onParseFailure: (AppFilterParserException) -> Unit
    ) {
        cpuBoundTaskExecutor.execute {
            val resources = appContext.packageManager.getResourcesForApplication(packageName)
            val icons = SparseArray<IconPackIconInfo>()
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
                                    var componentInfo: Component? = null
                                    var drawableName: String? = null
                                    for (i in 0 until parser.attributeCount) {
                                        when (parser.getAttributeName(i)) {
                                            "component" -> componentInfo =
                                                Component.parse(parser.getAttributeValue(i))
                                            "drawable" -> drawableName = parser.getAttributeValue(i)
                                        }
                                    }
                                    if (componentInfo != null && drawableName != null) {
                                        val id = resources.getIdentifier(drawableName, "drawable", packageName)
                                        if (id > 0 && icons[id] == null) {
                                            icons.put(
                                                id,
                                                IconPackIconInfo(
                                                    componentInfo,
                                                    drawableName,
                                                    Provider({ ResourcesCompat.getDrawable(resources, id, null) })
                                                )
                                            )
                                        }
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
                val iconList = ArrayList<IconPackIconInfo>(icons.size())
                icons.forEach { _, value -> iconList.add(value) }
                onParseSuccess(iconList)
            }
        }
    }

    private class AppFilterDoesNotExistException(packageName: String): Exception("Could not find AppFilter for $packageName")
    private class AppFilterParserException(packageName: String, e: Exception): Exception("Could not parse AppFilter for $packageName", e)

}