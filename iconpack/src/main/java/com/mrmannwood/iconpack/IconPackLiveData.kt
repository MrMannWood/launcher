package com.mrmannwood.iconpack

import android.content.ComponentName
import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class IconPackLiveData(
    context: Context,
    private val componentName: ComponentName,
    private val packageManagerExecutor: Executor,
    private val cpuBoundTaskExecutor: Executor,
    private val installedAppsLiveData: LiveData<List<ComponentName>>
) : LiveData<Result<Map<ComponentName, IconPackIconInfo>>>() {

    private val appContext = context.applicationContext
    private val isActive = AtomicBoolean(false)

    private val installedAppsObserver = Observer<List<ComponentName>> { installedApps ->
        if (!isActive.get()) return@Observer
        packageManagerExecutor.execute {
            if (!isActive.get()) return@execute
            val parser = getAppFilterParser()
            if (!isActive.get()) return@execute
            if (parser == null) {
                postValue(Result.failure(AppFilterDoesNotExistException(componentName)))
                return@execute
            }
            readAppFilter(
                parser = parser,
                installedApps = installedApps,
                onParseSuccess = { icons -> postValue(Result.success(icons)) },
                onParseFailure = { exception -> postValue(Result.failure(exception)) }
            )
        }
    }

    override fun onActive() {
        super.onActive()
        isActive.set(true)
        installedAppsLiveData.observeForever(installedAppsObserver)
    }

    override fun onInactive() {
        isActive.set(false)
        installedAppsLiveData.removeObserver(installedAppsObserver)
        super.onInactive()
    }

    private fun getAppFilterParser(): XmlPullParser? {
        val iconPackResources =
            appContext.packageManager.getResourcesForApplication(componentName.packageName)

        val id = iconPackResources.getIdentifier("appfilter", "xml", componentName.packageName)
        return if (id > 0) {
            iconPackResources.getXml(id)
        } else {
            try {
                XmlPullParserFactory.newInstance()
                    .apply { isNamespaceAware = true }
                    .newPullParser()
                    .apply { setInput(iconPackResources.assets.open("appfilter.xml"), "utf-8") }
            } catch (e: IOException) { null }
        }
    }

    private fun readAppFilter(
        parser: XmlPullParser,
        installedApps: List<ComponentName>,
        onParseSuccess: (Map<ComponentName, IconPackIconInfo>) -> Unit,
        onParseFailure: (AppFilterParserException) -> Unit
    ) {
        cpuBoundTaskExecutor.execute {
            val resources = appContext.packageManager.getResourcesForApplication(componentName.packageName)
            val icons = mutableMapOf<ComponentName, IconPackIconInfo>()
            var success = true
            try {
                while (true) {
                    when (parser.eventType) {
                        XmlPullParser.END_DOCUMENT -> break
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "iconback" -> println("02_MARSHALL: iconback")
                                "iconmask" -> println("02_MARSHALL: iconmask")
                                "iconupon" -> println("02_MARSHALL: iconupon")
                                "scale" -> println("02_MARSHALL: scale")
                                "item" -> {
                                    var componentInfo: ComponentName? = null
                                    var drawableName: String? = null
                                    for (i in 0 until parser.attributeCount) {
                                        when (parser.getAttributeName(i)) {
                                            "component" -> {
                                                componentInfo = parseComponentName(parser.getAttributeValue(i))
                                            }
                                            "drawable" -> drawableName = parser.getAttributeValue(i)
                                        }
                                    }
                                    if (componentInfo != null && drawableName != null && installedApps.contains(componentInfo)) {
                                        val id = resources.getIdentifier(drawableName, "drawable", componentName.packageName)
                                        if (id > 0) {
                                            icons[componentInfo] = IconPackIconInfo(
                                                componentInfo,
                                                drawableName
                                            ) { ResourcesCompat.getDrawable(resources, id, null) }
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
                onParseFailure(AppFilterParserException(componentName, e))
            }
            if (success) {
                onParseSuccess(icons)
            }
        }
    }

    private class AppFilterDoesNotExistException(componentName: ComponentName) : Exception("Could not find AppFilter for ${componentName.flattenToString()}")
    private class AppFilterParserException(componentName: ComponentName, e: Exception) : Exception("Could not parse AppFilter for ${componentName.flattenToString()}", e)
}
