package com.example.testapp

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ApplicationPackageLiveData(private val pacMan: PackageManager): LiveData<LoadResult<List<AppInfo>, Exception>>() {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onActive() {
        println("onActive")
        super.onActive()
        executor.submit {
            println("executor running")
            val value: LoadResult<List<AppInfo>, Exception> = try {
                val packages: List<PackageInfo> = pacMan.getInstalledPackages(0)

                val appList = mutableListOf<AppInfo>()
                for (pack in packages) {
                    if (isSystemPackage(pack)) {
                        continue
                    }
                    val a = AppInfo(
                        packageName = pack.applicationInfo.packageName,
                        icon = convertToGrayscale(pack.applicationInfo.loadIcon(pacMan)),
                        label = pack.applicationInfo.loadLabel(pacMan).toString()
                    )
                    appList.add(a)
                }
                appList.sortBy { it.label }

                LoadResult.Result(appList)
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
            postValue(value)
        }
    }

    override fun onInactive() {
        super.onInactive()
    }

    private fun isSystemPackage(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    private fun convertToGrayscale(drawable: Drawable): Drawable {
//        val matrix = ColorMatrix()
//        matrix.setSaturation(.05f)
//        val filter = ColorMatrixColorFilter(matrix)
//        drawable.colorFilter = filter
        return drawable
    }
}