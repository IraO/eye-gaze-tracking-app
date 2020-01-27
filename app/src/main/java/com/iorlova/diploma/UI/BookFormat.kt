package com.iorlova.diploma.UI
import android.content.ContentResolver
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.InputStream

enum class BookFormat(val format: String) {
    TXT("txt"),
    RTF("rtf"),
    PDF("pdf")
}

fun createNewFile(bookUri: Uri, contentResolver: ContentResolver): File {
    val file = File(Environment.getExternalStorageDirectory().toString() + "/" + File.separator + "tmpBook")
    val bookInputStream = contentResolver.openInputStream(bookUri)

    file.createNewFile()
    file.copyInputStreamToFile(bookInputStream!!)
    file.deleteOnExit()

    return file
}

private fun File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}
