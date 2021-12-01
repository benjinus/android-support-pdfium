package org.benjinus.pdfium

import java.util.*

data class Bookmark(
    val nativePtr: Long,
    var title: String? = null,
    var pageIdx: Long = 0,
    var children: MutableList<Bookmark> = ArrayList(),
    var level: Int = 0
) {

    private constructor() : this(-1) {}

    fun hasChildren(): Boolean {
        return !children.isEmpty()
    }
}