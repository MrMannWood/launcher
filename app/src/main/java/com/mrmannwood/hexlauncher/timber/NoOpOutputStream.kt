package com.mrmannwood.hexlauncher.timber

import java.io.OutputStream

object NoOpOutputStream : OutputStream() {
    override fun write(b: ByteArray?) { /* no-op */ }

    override fun write(b: ByteArray?, off: Int, len: Int) { /* no-op */ }

    override fun write(b: Int) { /* no-op */ }
}