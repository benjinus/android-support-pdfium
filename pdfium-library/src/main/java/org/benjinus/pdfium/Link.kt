package org.benjinus.pdfium

import android.graphics.RectF

class Link(bounds: RectF?, destPageIdx: Int?, uri: String?) {
    var bounds: RectF? = bounds
        private set
    var destPageIdx: Int? = destPageIdx
        private set
    var uri: String? = uri
        private set

}