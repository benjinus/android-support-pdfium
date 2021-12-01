package org.benjinus.pdfium

import java.util.*

class Bookmark(val nativePtr: Long) {
    var title: String? = null
    var pageIdx: Long = 0
    var children: MutableList<Bookmark> = ArrayList()

    private constructor() : this(-1) {}

    fun hasChildren(): Boolean {
        return !children.isEmpty()
    }
}