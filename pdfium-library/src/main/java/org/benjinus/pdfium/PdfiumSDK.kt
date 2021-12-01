package org.benjinus.pdfium

import android.graphics.*
import android.os.ParcelFileDescriptor
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.ArrayMap
import android.util.Log
import android.view.Surface
import org.benjinus.pdfium.search.FPDFTextSearchContext
import org.benjinus.pdfium.search.SearchData
import org.benjinus.pdfium.search.TextSearchContext
import org.benjinus.pdfium.util.FileUtils.getNumFd
import org.benjinus.pdfium.util.Size
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * pdfium has default dpi set to 72
 * Context needed to get screen density
 */

class PdfiumSDK(file: File, pdfPassword: String? = null) {
    val currentDpi = 72
    val mNativePagesPtr: MutableMap<Int, Long> = ArrayMap()
    val mNativeTextPagesPtr: MutableMap<Int, Long> = ArrayMap()
    val mNativeSearchHandlePtr: MutableMap<Int, Long> = ArrayMap()
    var mNativeDocPtr: Long = 0
    var mFileDescriptor: ParcelFileDescriptor? = null

    companion object {
        private val TAG = "PDFSDK"

        init {
            System.loadLibrary("pdfsdk")
            System.loadLibrary("pdfsdk_jni")
        }
    }

    init {
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        newDocument(fd = fileDescriptor, password = pdfPassword)
    }

    external fun nativeOpenDocument(fd: Int, password: String?): Long
    external fun nativeOpenMemDocument(data: ByteArray, password: String): Long
    external fun nativeCloseDocument(docPtr: Long)
    external fun nativeGetPageCount(docPtr: Long): Int
    external fun nativeLoadPage(docPtr: Long, pageIndex: Int): Long
    external fun nativeLoadPages(docPtr: Long, fromIndex: Int, toIndex: Int): LongArray
    external fun nativeClosePage(pagePtr: Long)
    external fun nativeClosePages(pagesPtr: LongArray)
    external fun nativeGetPageWidthPixel(pagePtr: Long, dpi: Int): Int
    external fun nativeGetPageHeightPixel(pagePtr: Long, dpi: Int): Int
    external fun nativeGetPageWidthPoint(pagePtr: Long): Int
    external fun nativeGetPageHeightPoint(pagePtr: Long): Int
    external fun nativeGetPageRotation(pagePtr: Long): Int
    external fun nativeRenderPage(
        pagePtr: Long, surface: Surface, dpi: Int,
        startX: Int, startY: Int,
        drawSizeHor: Int, drawSizeVer: Int,
        renderAnnot: Boolean
    )

    external fun nativeRenderPageBitmap(
        pagePtr: Long, bitmap: Bitmap, dpi: Int,
        startX: Int, startY: Int,
        drawSizeHor: Int, drawSizeVer: Int,
        renderAnnot: Boolean
    )

    external fun nativeGetDocumentMetaText(docPtr: Long, tag: String): String?
    external fun nativeGetFirstChildBookmark(docPtr: Long, bookmarkPtr: Long?): Long?
    external fun nativeGetSiblingBookmark(docPtr: Long, bookmarkPtr: Long): Long?
    external fun nativeGetBookmarkTitle(bookmarkPtr: Long): String?
    external fun nativeGetBookmarkDestIndex(docPtr: Long, bookmarkPtr: Long): Long
    external fun nativeGetPageSizeByIndex(docPtr: Long, pageIndex: Int, dpi: Int): Size
    external fun nativeGetPageLinks(pagePtr: Long): LongArray
    external fun nativeGetDestPageIndex(docPtr: Long, linkPtr: Long): Int?
    external fun nativeGetLinkURI(docPtr: Long, linkPtr: Long): String?
    external fun nativeGetLinkRect(linkPtr: Long): RectF?
    external fun nativePageCoordinateToDevice(
        pagePtr: Long, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, pageX: Double, pageY: Double
    ): Point

    external fun nativeDeviceCoordinateToPage(
        pagePtr: Long, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, deviceX: Int, deviceY: Int
    ): PointF

    ///////////////////////////////////////
    // PDF TextPage api
    ///////////
    external fun nativeLoadTextPage(docPtr: Long, pageIndex: Int): Long
    external fun nativeLoadTextPages(docPtr: Long, fromIndex: Int, toIndex: Int): LongArray
    external fun nativeCloseTextPage(pagePtr: Long)
    external fun nativeCloseTextPages(pagesPtr: LongArray)
    external fun nativeTextCountChars(textPagePtr: Long): Int
    external fun nativeTextGetText(
        textPagePtr: Long,
        start_index: Int,
        count: Int,
        result: ShortArray
    ): Int

    external fun nativeTextGetUnicode(textPagePtr: Long, index: Int): Int
    external fun nativeTextGetCharBox(textPagePtr: Long, index: Int): DoubleArray
    external fun nativeTextGetCharIndexAtPos(
        textPagePtr: Long,
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double
    ): Int

    external fun nativeTextCountRects(textPagePtr: Long, start_index: Int, count: Int): Int
    external fun nativeTextGetRect(textPagePtr: Long, rect_index: Int): DoubleArray
    external fun nativeTextGetBoundedTextLength(
        textPagePtr: Long,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double
    ): Int

    external fun nativeTextGetBoundedText(
        textPagePtr: Long,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        arr: ShortArray
    ): Int

    ///////////////////////////////////////
    // PDF Search API
    ///////////
    external fun nativeSearchStart(
        textPagePtr: Long,
        query: String,
        matchCase: Boolean,
        matchWholeWord: Boolean
    ): Long

    external fun nativeSearchStop(searchHandlePtr: Long)
    external fun nativeSearchNext(searchHandlePtr: Long): Boolean
    external fun nativeSearchPrev(searchHandlePtr: Long): Boolean
    external fun nativeGetCharIndexOfSearchResult(searchHandlePtr: Long): Int
    external fun nativeCountSearchResult(searchHandlePtr: Long): Int

    ///////////////////////////////////////
    // PDF Annotation API
    ///////////
    external fun nativeAddTextAnnotation(
        docPtr: Long,
        pageIndex: Int,
        text: String,
        color: IntArray,
        bound: IntArray
    ): Long

    ///////////////////////////////////////
    // PDF Native Callbacks
    ///////////
    fun onAnnotationAdded(pageIndex: Int, pageNewPtr: Long) {}
    fun onAnnotationUpdated(pageIndex: Int, pageNewPtr: Long) {}
    fun onAnnotationRemoved(pageIndex: Int, pageNewPtr: Long) {}
    ///////////////////////////////////////
    // PDF SDK functions
    ///////////

    /**
     * Create new document from file with password
     */
    @Synchronized
    @Throws(IOException::class)
    private fun newDocument(fd: ParcelFileDescriptor, password: String? = null) {
        mFileDescriptor = fd
        val numFd = getNumFd(fd)
        mNativeDocPtr = nativeOpenDocument(numFd, password)
    }

    /**
     * Get total numer of pages in document
     */
    val pageCount: Int
        get() {
            return nativeGetPageCount(mNativeDocPtr)
        }

    /**
     * Open page and store native pointer
     */
    fun openPage(pageIndex: Int): Long {
        val pagePtr: Long
        pagePtr = nativeLoadPage(mNativeDocPtr, pageIndex)
        mNativePagesPtr[pageIndex] = pagePtr
        prepareTextInfo(pageIndex)
        return pagePtr
    }

    /**
     * Open range of pages and store native pointers
     */
    fun openPage(fromIndex: Int, toIndex: Int): LongArray {
        val pagesPtr: LongArray
        pagesPtr = nativeLoadPages(mNativeDocPtr, fromIndex, toIndex)
        var pageIndex = fromIndex
        for (page in pagesPtr) {
            if (pageIndex > toIndex) break
            mNativePagesPtr[pageIndex] = page
            pageIndex++
            prepareTextInfo(pageIndex)
        }
        return pagesPtr
    }

    /**
     * Get page width in pixels. <br></br>
     * This method requires page to be opened.
     */
    fun getPageWidth(index: Int): Int {
        var pagePtr: Long = -1L
        return if (mNativePagesPtr[index]?.also { pagePtr = it } != null) {
            nativeGetPageWidthPixel(pagePtr, this.currentDpi)
        } else 0
    }

    /**
     * Get page height in pixels. <br></br>
     * This method requires page to be opened.
     */
    fun getPageHeight(index: Int): Int {
        var pagePtr: Long = -1L
        return if (mNativePagesPtr[index]?.also { pagePtr = it } != null) {
            nativeGetPageHeightPixel(pagePtr, this.currentDpi)
        } else 0
    }

    /**
     * Get page width in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
     */
    fun getPageWidthPoint(index: Int): Int {
        var pagePtr: Long = -1L
        return if (mNativePagesPtr[index]?.also { pagePtr = it } != null) {
            nativeGetPageWidthPoint(pagePtr)
        } else 0
    }

    /**
     * Get page height in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
     */
    fun getPageHeightPoint(index: Int): Int {
        var pagePtr: Long = -1L
        return if (mNativePagesPtr[index]?.also { pagePtr = it } != null) {
            nativeGetPageHeightPoint(pagePtr)
        } else 0
    }

    /**
     * Get size of page in pixels.<br></br>
     * This method does not require given page to be opened.
     */
    fun getPageSize(index: Int): Size {
        return nativeGetPageSizeByIndex(mNativeDocPtr, index, this.currentDpi)
    }

    /**
     * Get the rotation of page<br></br>
     */
    fun getPageRotation(index: Int): Int {
        var pagePtr: Long = -1L
        return if (mNativePagesPtr[index]?.also { pagePtr = it } != null) {
            nativeGetPageRotation(pagePtr)
        } else 0
    }
    /**
     * Render page fragment on [Surface]. This method allows to render annotations.<br></br>
     * Page must be opened before rendering.
     */
    /**
     * Render page fragment on [Surface].<br></br>
     * Page must be opened before rendering.
     */
    @JvmOverloads
    fun renderPage(
        surface: Surface, pageIndex: Int,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int,
        renderAnnot: Boolean = false
    ) {
        try {
            //nativeRenderPage(mNativePagesPtr.get(pageIndex), surface, mCurrentDpi);
            mNativePagesPtr[pageIndex]?.let {
                nativeRenderPage(
                    it, surface, this.currentDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot
                )
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "mContext may be null")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Exception throw from native")
            e.printStackTrace()
        }
    }
    /**
     * Render page fragment on [Bitmap]. This method allows to render annotations.<br></br>
     * Page must be opened before rendering.
     *
     *
     * For more info see [PdfiumSDK.renderPageBitmap]
     */
    /**
     * Render page fragment on [Bitmap].<br></br>
     * Page must be opened before rendering.
     *
     *
     * Supported bitmap configurations:
     *
     *  * ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
     *  * RGB_565 - little worse quality, twice less memory usage
     *
     */
    @JvmOverloads
    fun renderPageBitmap(
        bitmap: Bitmap, pageIndex: Int,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int,
        renderAnnot: Boolean = false
    ) {
        try {
            mNativePagesPtr[pageIndex]?.let {
                nativeRenderPageBitmap(
                    it, bitmap, this.currentDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot
                )
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "mContext may be null")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Exception throw from native")
            e.printStackTrace()
        }
    }

    /**
     * Release native resources and opened file
     */
    fun closeDocument() {
        for (index in mNativePagesPtr.keys) {
            mNativePagesPtr[index]?.let { nativeClosePage(it) }
        }
        mNativePagesPtr.clear()
        for (ptr in mNativeTextPagesPtr.keys) {
            mNativeTextPagesPtr[ptr]?.let { nativeCloseTextPage(it) }
        }
        mNativeTextPagesPtr.clear()
        nativeCloseDocument(mNativeDocPtr)
        if (mFileDescriptor != null) {
            try {
                mFileDescriptor?.close()
                mFileDescriptor = null
            } catch (ignored: IOException) {
            } finally {
                mFileDescriptor = null
            }
        }
    }

    /**
     * Get metadata for given document
     */
    val documentMeta: Meta
        get() {
            return Meta(
                nativeGetDocumentMetaText(mNativeDocPtr, "Title"),
                nativeGetDocumentMetaText(mNativeDocPtr, "Author"),
                nativeGetDocumentMetaText(mNativeDocPtr, "Subject"),
                nativeGetDocumentMetaText(mNativeDocPtr, "Keywords"),
                nativeGetDocumentMetaText(mNativeDocPtr, "Creator"),
                nativeGetDocumentMetaText(mNativeDocPtr, "Producer"),
                nativeGetDocumentMetaText(mNativeDocPtr, "CreationDate"),
                nativeGetDocumentMetaText(mNativeDocPtr, "ModDate")
            )
        }

    /**
     * Get table of contents (bookmarks) for given document
     */
    val tableOfContents: List<Bookmark>
        get() {
            val topLevel: MutableList<Bookmark> = ArrayList()
            val first = nativeGetFirstChildBookmark(mNativeDocPtr, null)
            first?.let { recursiveGetBookmark(topLevel, it) }
            return topLevel
        }

    fun recursiveGetBookmark(tree: MutableList<Bookmark>, bookmarkPtr: Long) {
        val bookmark = Bookmark(bookmarkPtr)
        bookmark.title = nativeGetBookmarkTitle(bookmarkPtr)
        bookmark.pageIdx = nativeGetBookmarkDestIndex(mNativeDocPtr, bookmarkPtr)
        tree.add(bookmark)
        val child = nativeGetFirstChildBookmark(mNativeDocPtr, bookmarkPtr)
        if (child != null) {
            recursiveGetBookmark(bookmark.children, child)
        }
        val sibling = nativeGetSiblingBookmark(mNativeDocPtr, bookmarkPtr)
        sibling?.let { recursiveGetBookmark(tree, it) }
    }

    /**
     * Get all links from given page
     */
    fun getPageLinks(pageIndex: Int): List<Link> {
        val links: MutableList<Link> = ArrayList()
        val nativePagePtr = mNativePagesPtr[pageIndex] ?: return links
        val linkPtrs = nativeGetPageLinks(nativePagePtr)
        for (linkPtr in linkPtrs) {
            val index = nativeGetDestPageIndex(mNativeDocPtr, linkPtr)
            val uri = nativeGetLinkURI(mNativeDocPtr, linkPtr)
            val rect = nativeGetLinkRect(linkPtr)
            if (rect != null && (index != null || uri != null)) {
                links.add(Link(rect, index, uri))
            }
        }
        return links
    }

    /**
     * Map page coordinates to device screen coordinates
     *
     * @param pageIndex index of page
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 90 degrees counter-clockwise)
     * @param pageX     X value in page coordinates
     * @param pageY     Y value in page coordinate
     * @return mapped coordinates
     */
    fun mapPageCoordsToDevice(
        pageIndex: Int, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, pageX: Double, pageY: Double
    ): Point? {
        val pagePtr = mNativePagesPtr[pageIndex]
        return pagePtr?.let {
            nativePageCoordinateToDevice(
                it,
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                pageX,
                pageY
            )
        }
    }

    /**
     * Convert the screen coordinates of a point to page coordinates.
     *
     *
     * The page coordinate system has its origin at the left-bottom corner
     * of the page, with the X-axis on the bottom going to the right, and
     * the Y-axis on the left side going up.
     *
     *
     * NOTE: this coordinate system can be altered when you zoom, scroll,
     * or rotate a page, however, a point on the page should always have
     * the same coordinate values in the page coordinate system.
     *
     *
     * The device coordinate system is device dependent. For screen device,
     * its origin is at the left-top corner of the window. However this
     * origin can be altered by the Windows coordinate transformation
     * utilities.
     *
     *
     * You must make sure the start_x, start_y, size_x, size_y
     * and rotate parameters have exactly same values as you used in
     * the FPDF_RenderPage() function call.
     *
     * @param pageIndex index of page
     * @param startX    Left pixel position of the display area in device coordinates.
     * @param startY    Top pixel position of the display area in device coordinates.
     * @param sizeX     Horizontal size (in pixels) for displaying the page.
     * @param sizeY     Vertical size (in pixels) for displaying the page.
     * @param rotate    Page orientation:
     * 0 (normal)
     * 1 (rotated 90 degrees clockwise)
     * 2 (rotated 180 degrees)
     * 3 (rotated 90 degrees counter-clockwise)
     * @param deviceX   X value in device coordinates to be converted.
     * @param deviceY   Y value in device coordinates to be converted.
     */
    fun mapDeviceCoordinateToPage(
        pageIndex: Int, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, deviceX: Int, deviceY: Int
    ): PointF? {
        val pagePtr = mNativePagesPtr[pageIndex]
        return pagePtr?.let {
            nativeDeviceCoordinateToPage(
                it,
                startX,
                startY,
                sizeX,
                sizeY,
                rotate,
                deviceX,
                deviceY
            )
        }
    }

    /**
     * @return mapped coordinates
     */
    fun mapPageCoordinateToDevice(
        pageIndex: Int, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, coords: RectF
    ): RectF? {
        val leftTop = mapPageCoordsToDevice(
            pageIndex, startX, startY, sizeX, sizeY, rotate,
            coords.left.toDouble(), coords.top.toDouble()
        )
        val rightBottom = mapPageCoordsToDevice(
            pageIndex, startX, startY, sizeX, sizeY, rotate,
            coords.right.toDouble(), coords.bottom.toDouble()
        )

        return leftTop?.let { lt ->
            rightBottom?.let { rb ->
                RectF(
                    lt.x.toFloat(),
                    lt.y.toFloat(),
                    rb.x.toFloat(),
                    rb.y.toFloat()
                )
            }
        }
    }
    ///////////////////////////////////////
    // FPDF_TEXTPAGE api
    ///////////
    /**
     * Prepare information about all characters in a page.
     * Application must call FPDFText_ClosePage to release the text page information.
     *
     * @param pageIndex index of page.
     * @return A handle to the text page information structure. NULL if something goes wrong.
     */
    fun prepareTextInfo(pageIndex: Int): Long {
        val textPagePtr: Long
        textPagePtr = nativeLoadTextPage(mNativeDocPtr, pageIndex)
        if (validPtr(textPagePtr)) {
            mNativeTextPagesPtr[pageIndex] = textPagePtr
        }
        return textPagePtr
    }

    /**
     * Release all resources allocated for a text page information structure.
     *
     * @param pageIndex index of page.
     */
    fun releaseTextInfo(pageIndex: Int) {
        val textPagePtr: Long = mNativeTextPagesPtr[pageIndex] ?: -1L
        if (validPtr(textPagePtr)) {
            nativeCloseTextPage(textPagePtr)
        }
    }

    /**
     * Prepare information about all characters in a range of pages.
     * Application must call FPDFText_ClosePage to release the text page information.
     *
     * @param fromIndex start index of page.
     * @param toIndex   end index of page.
     * @return list of handles to the text page information structure. NULL if something goes wrong.
     */
    fun prepareTextInfo(fromIndex: Int, toIndex: Int): LongArray {
        val textPagesPtr: LongArray
        textPagesPtr = nativeLoadTextPages(mNativeDocPtr, fromIndex, toIndex)
        var pageIndex = fromIndex
        for (page in textPagesPtr) {
            if (pageIndex > toIndex) break
            if (validPtr(page)) {
                mNativeTextPagesPtr[pageIndex] = page
            }
            pageIndex++
        }
        return textPagesPtr
    }

    /**
     * Release all resources allocated for a text page information structure.
     *
     * @param fromIndex start index of page.
     * @param toIndex   end index of page.
     */
    fun releaseTextInfo(fromIndex: Int, toIndex: Int) {
        var textPagesPtr: Long
        for (i in fromIndex until toIndex + 1) {
            textPagesPtr = mNativeTextPagesPtr[i] ?: -1L
            if (validPtr(textPagesPtr)) {
                nativeCloseTextPage(textPagesPtr)
            }
        }
    }

    fun ensureTextPage(pageIndex: Int): Long? {
        val ptr = mNativeTextPagesPtr[pageIndex]
        return if (!validPtr(ptr)) {
            prepareTextInfo(pageIndex)
        } else ptr
    }

    fun countCharactersOnPage(pageIndex: Int): Int {
        return try {
            val ptr = ensureTextPage(pageIndex)
            if (ptr != null && validPtr(ptr)) nativeTextCountChars(ptr) else 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Extract unicode text string from the page.
     *
     * @param pageIndex  index of page.
     * @param startIndex Index for the start characters.
     * @param length     Number of characters to be extracted.
     * @return Number of characters written into the result buffer, including the trailing terminator.
     */
    fun extractCharacters(pageIndex: Int, startIndex: Int, length: Int): String? {
        return try {
            val ptr = ensureTextPage(pageIndex)
            if (!validPtr(ptr)) {
                return null
            }
            val buf = ShortArray(length + 1)
            val r = ptr?.let { nativeTextGetText(it, startIndex, length, buf) } ?: 0
            if (r > 1) {
                val bytes = ByteArray((r - 1) * 2)
                val bb = ByteBuffer.wrap(bytes)
                bb.order(ByteOrder.LITTLE_ENDIAN)
                for (i in 0 until r - 1) {
                    val s = buf[i]
                    bb.putShort(s)
                }
                String(bytes, Charsets.UTF_16LE)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get Unicode of a character in a page.
     *
     * @param pageIndex index of page.
     * @param index     Zero-based index of the character.
     * @return The Unicode of the particular character. If a character is not encoded in Unicode, the return value will be zero.
     */
    fun extractCharacter(pageIndex: Int, index: Int): Char {
        return try {
            val ptr = ensureTextPage(pageIndex)
            if (ptr != null && validPtr(ptr)) nativeTextGetUnicode(
                ptr,
                index
            ).toChar() else 0.toChar()
        } catch (e: Exception) {
            0.toChar()
        }
    }

    /**
     * Get bounding box of a particular character.
     *
     * @param pageIndex index of page.
     * @param index     Zero-based index of the character.
     * @return the character position measured in PDF "user space".
     */
    fun measureCharacterBox(pageIndex: Int, index: Int): RectF? {
        return try {
            val ptr = ensureTextPage(pageIndex)
            if (ptr == null || !validPtr(ptr)) {
                return null
            }
            val o = nativeTextGetCharBox(ptr, index)
            val r = RectF()
            r.left = o[0].toFloat()
            r.right = o[1].toFloat()
            r.bottom = o[2].toFloat()
            r.top = o[3].toFloat()
            r
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get the index of a character at or nearby a certain position on the page
     *
     * @param pageIndex  index of page.
     * @param x          X position in PDF "user space".
     * @param y          Y position in PDF "user space".
     * @param xTolerance An x-axis tolerance value for character hit detection, in point unit.
     * @param yTolerance A y-axis tolerance value for character hit detection, in point unit.
     * @return The zero-based index of the character at, or nearby the point (x,y). If there is no character at or nearby the point, return value will be -1. If an error occurs, -3 will be returned.
     */
    fun getCharacterIndex(
        pageIndex: Int,
        x: Double,
        y: Double,
        xTolerance: Double,
        yTolerance: Double
    ): Int {
        return try {
            val ptr = ensureTextPage(pageIndex)
            if (validPtr(ptr)) ptr?.let {
                nativeTextGetCharIndexAtPos(
                    it,
                    x,
                    y,
                    xTolerance,
                    yTolerance
                )
            } ?: -1 else -1
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Count number of rectangular areas occupied by a segment of texts.
     *
     *
     * This function, along with FPDFText_GetRect can be used by applications to detect the position
     * on the page for a text segment, so proper areas can be highlighted or something.
     * FPDFTEXT will automatically merge small character boxes into bigger one if those characters
     * are on the same line and use same font settings.
     *
     * @param pageIndex index of page.
     * @param charIndex Index for the start characters.
     * @param count     Number of characters.
     * @return texts areas count.
     */
    fun countTextRect(pageIndex: Int, charIndex: Int, count: Int): Int {
        return try {
            val ptr = ensureTextPage(pageIndex)
            if (ptr != null && validPtr(ptr)) nativeTextCountRects(ptr, charIndex, count) else -1
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * Get a rectangular area from the result generated by FPDFText_CountRects.
     *
     * @param pageIndex index of page.
     * @param rectIndex Zero-based index for the rectangle.
     * @return the text rectangle.
     */
    fun getTextRect(pageIndex: Int, rectIndex: Int): RectF? {
        return try {
            val ptr = ensureTextPage(pageIndex)
            if (!validPtr(ptr)) {
                return null
            }
            val o = ptr?.let { nativeTextGetRect(it, rectIndex) }
            val r = RectF()
            if (o != null) {
                r.left = o[0].toFloat()
                r.top = o[1].toFloat()
                r.right = o[2].toFloat()
                r.bottom = o[3].toFloat()
                r
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract unicode text within a rectangular boundary on the page.
     * If the buffer is too small, as much text as will fit is copied into it.
     *
     * @param pageIndex index of page.
     * @param rect      the text rectangle to extract.
     * @return If buffer is NULL or buflen is zero, return number of characters (not bytes) of text
     * present within the rectangle, excluding a terminating NUL.
     *
     *
     * Generally you should pass a buffer at least one larger than this if you want a terminating NUL,
     * which will be provided if space is available. Otherwise, return number of characters copied
     * into the buffer, including the terminating NUL  when space for it is available.
     */
    fun extractText(pageIndex: Int, rect: RectF): String? {
        return try {
            val ptr = ensureTextPage(pageIndex)
            if (ptr == null || !validPtr(ptr)) {
                return null
            }
            val length = nativeTextGetBoundedTextLength(
                ptr,
                rect.left.toDouble(),
                rect.top.toDouble(),
                rect.right.toDouble(),
                rect.bottom.toDouble()
            )
            if (length <= 0) {
                return null
            }
            val buf = ShortArray(length + 1)
            val r = nativeTextGetBoundedText(
                ptr,
                rect.left.toDouble(),
                rect.top.toDouble(),
                rect.right.toDouble(),
                rect.bottom.toDouble(),
                buf
            )
            val bytes = ByteArray((r - 1) * 2)
            val bb = ByteBuffer.wrap(bytes)
            bb.order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until r - 1) {
                val s = buf[i]
                bb.putShort(s)
            }
            String(bytes, Charsets.UTF_16LE)
        } catch (e: Exception) {
            null
        }
    }

    fun validPtr(ptr: Long?): Boolean {
        return ptr != null && ptr != -1L
    }

    /**
     * A handle class for the search context. stopSearch must be called to release this handle.
     *
     * @param pageIndex      index of page.
     * @param query          A unicode match pattern.
     * @param matchCase      match case
     * @param matchWholeWord match the whole word
     * @return A handle for the search context.
     */
    fun newPageSearch(
        pageIndex: Int,
        query: String,
        matchCase: Boolean,
        matchWholeWord: Boolean
    ): TextSearchContext {
        return object : FPDFTextSearchContext(pageIndex, query, matchCase, matchWholeWord) {
            private var mSearchHandlePtr: Long? = null
            override fun prepareSearch() {
                val textPage = prepareTextInfo(pageIndex)
                if (hasSearchHandle(pageIndex)) {
                    val sPtr = mNativeSearchHandlePtr[pageIndex]
                    sPtr?.let { nativeSearchStop(it) }
                }
                mSearchHandlePtr = nativeSearchStart(
                    textPage, query, isMatchCase,
                    isMatchWholeWord
                )
            }

            override fun countResult(): Int {
                return if (validPtr(mSearchHandlePtr)) {
                    mSearchHandlePtr?.let { nativeCountSearchResult(it) } ?: -1
                } else -1
            }

            override fun searchNext(): RectF? {
                if (validPtr(mSearchHandlePtr)) {
                    mHasNext = mSearchHandlePtr?.let { nativeSearchNext(it) } == true
                    if (mHasNext) {
                        val index =
                            mSearchHandlePtr?.let { nativeGetCharIndexOfSearchResult(it) } ?: -1
                        if (index > -1) {
                            return measureCharacterBox(this.pageIndex, index)
                        }
                    }
                }
                mHasNext = false
                return null
            }

            override fun searchPrev(): RectF? {
                if (validPtr(mSearchHandlePtr)) {
                    mHasPrev = mSearchHandlePtr?.let { nativeSearchPrev(it) } == true
                    if (mHasPrev) {
                        val index =
                            mSearchHandlePtr?.let { nativeGetCharIndexOfSearchResult(it) } ?: -1
                        if (index > -1) {
                            return measureCharacterBox(this.pageIndex, index)
                        }
                    }
                }
                mHasPrev = false
                return null
            }

            override fun stopSearch() {
                super.stopSearch()
                if (validPtr(mSearchHandlePtr)) {
                    mSearchHandlePtr?.let { nativeSearchStop(it) }
                    mNativeSearchHandlePtr.remove(pageIndex)
                }
            }
        }
    }

    fun hasPage(index: Int): Boolean {
        return mNativePagesPtr.containsKey(index)
    }

    fun hasTextPage(index: Int): Boolean {
        return mNativeTextPagesPtr.containsKey(index)
    }

    fun hasSearchHandle(index: Int): Boolean {
        return mNativeSearchHandlePtr.containsKey(index)
    }

    fun getChapters(): List<Bookmark> = tableOfContents
    fun getAllChaptersOneLevel(): List<Bookmark> = getChapters().getFlattenChapters()

    private fun List<Bookmark>.getFlattenChapters(level: Int = 0): List<Bookmark> {
        val flatten = mutableListOf<Bookmark>()
        this.forEach {
            flatten.add(it)
            if (it.children.isNotEmpty()) {
                flatten += it.children.getFlattenChapters(level = level+1)
            }
        }
        return flatten
    }

    fun getChapter(byPageIndex: Long): Bookmark? {
        val chapters = getChapters()
        val fir = chapters.firstOrNull { it.pageIdx == byPageIndex }
        if (fir != null) {
            return fir
        } else {
            val chaptersFlatten = chapters.getFlattenChapters()
            val first1 = chaptersFlatten.firstOrNull { it.pageIdx == byPageIndex }
            when {
                first1 != null -> {
                    return first1
                }
                chaptersFlatten.size >= 2 -> {
                    val fff = chaptersFlatten.toMutableList().zipWithNext { a, b -> a to b }
                    for (element in fff) {
                        val first = element.first
                        val second = element.second
                        val ind = if (first.pageIdx > 0L)
                            first.pageIdx - 1L
                        else first.pageIdx

                        if (byPageIndex in ind..second.pageIdx) {
                            return first
                        }
                    }

                    return chaptersFlatten.lastOrNull()
                }
                else -> return null
            }
        }
    }

    fun search(searchStr: String, ignoreCase: Boolean = true): List<SearchData> {
        if (mFileDescriptor == null || mFileDescriptor?.fd ?: -1 <= 0) {
            return emptyList()
        }
        val rList = ArrayList<SearchData>()
        for (i in 0 until pageCount) {
            extractCharacters(i, 0, countCharactersOnPage(pageIndex = i))?.let { chars ->
                if (chars.contains(other = searchStr, ignoreCase = ignoreCase)) {
                    val sentences: List<String> = chars.split(".")
                        .filter { it.contains(other = searchStr, ignoreCase = ignoreCase) }
                    if (sentences.isNotEmpty()) {
                        val chapter = getChapter(byPageIndex = i.toLong())
                        sentences.forEach { s ->
                            val spannable = SpannableString(s.trim().replace("\n", " "))
                            spannable.setSpan(
                                StyleSpan(Typeface.NORMAL),
                                0, spannable.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            val fInd =
                                spannable.indexOf(string = searchStr, ignoreCase = ignoreCase)
                            if (fInd > -1) {
                                val lInd = fInd + searchStr.length
                                if (lInd < spannable.length) {
                                    spannable.setSpan(
                                        StyleSpan(Typeface.BOLD),
                                        fInd, lInd,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                            }
                            rList += SearchData(
                                chapter = chapter,
                                pageNumber = i,
                                partOfText = spannable
                            )
                        }
                    }
                }
            }
        }
        return rList
    }
}