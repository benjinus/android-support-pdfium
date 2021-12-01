package org.benjinus.pdfium.search

import android.text.Spannable
import org.benjinus.pdfium.Bookmark

data class SearchData(
    val chapter: Bookmark? = null,
    /**
     * number of page in range from 1 to pagesCount
     */
    val pageNumber: Int,
    val partOfText: Spannable
)
