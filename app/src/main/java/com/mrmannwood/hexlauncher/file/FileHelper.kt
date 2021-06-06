package com.mrmannwood.hexlauncher.file

import java.io.File
import java.io.IOException
import java.nio.file.Files

@Throws(IOException::class)
fun File.copyContentsTo(dest: File) : List<File> {
    if (!dest.exists()) dest.mkdirs()

    val files = mutableListOf<File>()
    for (file in listFiles() ?: emptyArray()) {
        val result = File(dest, file.name)
        if (!result.createNewFile()) continue
        Files.copy(file.toPath(), result.toPath())
        files.add(result)
    }
    return files
}