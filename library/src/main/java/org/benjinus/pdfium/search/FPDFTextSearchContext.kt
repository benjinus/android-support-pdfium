package org.benjinus.pdfium.search


abstract class FPDFTextSearchContext constructor(
    override val pageIndex: Int,
    override val query: String,
    override val isMatchCase: Boolean,
    override val isMatchWholeWord: Boolean
) : TextSearchContext {
    @JvmField
    protected var mHasNext = true

    @JvmField
    protected var mHasPrev = false

    override fun hasNext(): Boolean {
        return countResult() > 0 || mHasNext
    }

    override fun hasPrev(): Boolean {
        return countResult() > 0 || mHasPrev
    }

    override fun startSearch() {
        searchNext()
    }

    override fun stopSearch() {}

    init {
        prepareSearch()
    }
}