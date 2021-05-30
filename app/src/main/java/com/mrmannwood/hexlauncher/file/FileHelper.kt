package com.mrmannwood.hexlauncher.file

import java.io.*

fun File.copyContentsTo(dest: File) : List<File> {
    if (!dest.exists()) dest.mkdirs()

    val files = mutableListOf<File>()
    for (file in listFiles() ?: emptyArray()) {
        val result = File(dest, file.name)
        if (!result.createNewFile()) continue
        BufferedReader(FileReader(file)).use { reader ->
            OutputStreamWriter(FileOutputStream(result)).use { writer ->
                while (true) {
                    val line = reader.readLine() ?: break
                    writer.write(line)
                    writer.write("\n")
                }
            }
        }
        files.add(result)
    }
    return files
}