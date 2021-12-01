package org.benjinus.pdfium.search

import android.graphics.RectF

interface TextSearchContext {
    fun prepareSearch()
    val pageIndex: Int
    val query: String?
    val isMatchCase: Boolean
    val isMatchWholeWord: Boolean
    fun countResult(): Int
    operator fun hasNext(): Boolean
    fun hasPrev(): Boolean
    fun searchNext(): RectF?
    fun searchPrev(): RectF?
    fun startSearch()
    fun stopSearch()
}