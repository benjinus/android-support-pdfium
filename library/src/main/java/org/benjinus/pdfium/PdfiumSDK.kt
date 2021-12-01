package org.benjinus.pdfium;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.ParcelFileDescriptor;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Surface;

import org.benjinus.pdfium.search.FPDFTextSearchContext;
import org.benjinus.pdfium.search.TextSearchContext;
import org.benjinus.pdfium.util.FileUtils;
import org.benjinus.pdfium.util.Size;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("JniMissingFunction")
public class PdfiumSDK {

    private static final String TAG = "PDFSDK";

    private int mCurrentDpi;
    private final Map<Integer, Long> mNativePagesPtr = new ArrayMap<>();
    private final Map<Integer, Long> mNativeTextPagesPtr = new ArrayMap<>();
    private final Map<Integer, Long> mNativeSearchHandlePtr = new ArrayMap<>();
    private long mNativeDocPtr;
    private ParcelFileDescriptor mFileDescriptor;

    static {
        System.loadLibrary("pdfsdk");
        System.loadLibrary("pdfsdk_jni");
    }

    /**
     * Context needed to get screen density
     */
    public PdfiumSDK() {
        mCurrentDpi = 72; // pdfium has default dpi set to 72
    }

    private native long nativeOpenDocument(int fd, String password);

    private native long nativeOpenMemDocument(byte[] data, String password);

    private native void nativeCloseDocument(long docPtr);

    private native int nativeGetPageCount(long docPtr);

    private native long nativeLoadPage(long docPtr, int pageIndex);

    private native long[] nativeLoadPages(long docPtr, int fromIndex, int toIndex);

    private native void nativeClosePage(long pagePtr);

    private native void nativeClosePages(long[] pagesPtr);

    private native int nativeGetPageWidthPixel(long pagePtr, int dpi);

    private native int nativeGetPageHeightPixel(long pagePtr, int dpi);

    private native int nativeGetPageWidthPoint(long pagePtr);

    private native int nativeGetPageHeightPoint(long pagePtr);

    private native int nativeGetPageRotation(long pagePtr);

    private native void nativeRenderPage(long pagePtr, Surface surface, int dpi,
                                         int startX, int startY,
                                         int drawSizeHor, int drawSizeVer,
                                         boolean renderAnnot);

    private native void nativeRenderPageBitmap(long pagePtr, Bitmap bitmap, int dpi,
                                               int startX, int startY,
                                               int drawSizeHor, int drawSizeVer,
                                               boolean renderAnnot);

    private native String nativeGetDocumentMetaText(long docPtr, String tag);

    private native Long nativeGetFirstChildBookmark(long docPtr, Long bookmarkPtr);

    private native Long nativeGetSiblingBookmark(long docPtr, long bookmarkPtr);

    private native String nativeGetBookmarkTitle(long bookmarkPtr);

    private native long nativeGetBookmarkDestIndex(long docPtr, long bookmarkPtr);

    private native Size nativeGetPageSizeByIndex(long docPtr, int pageIndex, int dpi);

    private native long[] nativeGetPageLinks(long pagePtr);

    private native Integer nativeGetDestPageIndex(long docPtr, long linkPtr);

    private native String nativeGetLinkURI(long docPtr, long linkPtr);

    private native RectF nativeGetLinkRect(long linkPtr);

    private native Point nativePageCoordinateToDevice(long pagePtr, int startX, int startY, int sizeX,
                                                      int sizeY, int rotate, double pageX, double pageY);

    private native PointF nativeDeviceCoordinateToPage(long pagePtr, int startX, int startY, int sizeX,
                                                       int sizeY, int rotate, int deviceX, int deviceY);

    ///////////////////////////////////////
    // PDF TextPage api
    ///////////
    private native long nativeLoadTextPage(long docPtr, int pageIndex);

    private native long[] nativeLoadTextPages(long docPtr, int fromIndex, int toIndex);

    private native void nativeCloseTextPage(long pagePtr);

    private native void nativeCloseTextPages(long[] pagesPtr);

    private native int nativeTextCountChars(long textPagePtr);

    private native int nativeTextGetText(long textPagePtr, int start_index, int count, short[] result);

    private native int nativeTextGetUnicode(long textPagePtr, int index);

    private native double[] nativeTextGetCharBox(long textPagePtr, int index);

    private native int nativeTextGetCharIndexAtPos(long textPagePtr, double x, double y, double xTolerance, double yTolerance);

    private native int nativeTextCountRects(long textPagePtr, int start_index, int count);

    private native double[] nativeTextGetRect(long textPagePtr, int rect_index);

    private native int nativeTextGetBoundedTextLength(long textPagePtr, double left, double top, double right, double bottom);

    private native int nativeTextGetBoundedText(long textPagePtr, double left, double top, double right, double bottom, short[] arr);


    ///////////////////////////////////////
    // PDF Search API
    ///////////

    private native long nativeSearchStart(long textPagePtr, String query, boolean matchCase, boolean matchWholeWord);

    private native void nativeSearchStop(long searchHandlePtr);

    private native boolean nativeSearchNext(long searchHandlePtr);

    private native boolean nativeSearchPrev(long searchHandlePtr);

    private native int nativeGetCharIndexOfSearchResult(long searchHandlePtr);

    private native int nativeCountSearchResult(long searchHandlePtr);

    ///////////////////////////////////////
    // PDF Annotation API
    ///////////

    private native long nativeAddTextAnnotation(long docPtr, int pageIndex, String text, int[] color, int[] bound);

    ///////////////////////////////////////
    // PDF Native Callbacks
    ///////////

    private void onAnnotationAdded(int pageIndex, long pageNewPtr) {
    }

    private void onAnnotationUpdated(int pageIndex, long pageNewPtr) {
    }

    private void onAnnotationRemoved(int pageIndex, long pageNewPtr) {
    }

    ///////////////////////////////////////
    // PDF SDK functions
    ///////////

    /**
     * Create new document from file
     */
    public void newDocument(ParcelFileDescriptor fd) throws IOException {
        newDocument(fd, null);
    }

    /**
     * Create new document from file with password
     */
    public synchronized void newDocument(ParcelFileDescriptor fd, String password) throws IOException {
        mFileDescriptor = fd;
        int numFd = FileUtils.getNumFd(fd);
        mNativeDocPtr = nativeOpenDocument(numFd, password);
    }

    /**
     * Get total numer of pages in document
     */
    public int getPageCount() {
        return nativeGetPageCount(mNativeDocPtr);

    }

    /**
     * Open page and store native pointer
     */
    public long openPage(int pageIndex) {
        long pagePtr;
        pagePtr = nativeLoadPage(mNativeDocPtr, pageIndex);
        mNativePagesPtr.put(pageIndex, pagePtr);
        prepareTextInfo(pageIndex);
        return pagePtr;

    }

    /**
     * Open range of pages and store native pointers
     */
    public long[] openPage(int fromIndex, int toIndex) {
        long[] pagesPtr;
        pagesPtr = nativeLoadPages(mNativeDocPtr, fromIndex, toIndex);
        int pageIndex = fromIndex;
        for (long page : pagesPtr) {
            if (pageIndex > toIndex) break;
            mNativePagesPtr.put(pageIndex, page);
            pageIndex++;

            prepareTextInfo(pageIndex);
        }

        return pagesPtr;
    }

    /**
     * Get page width in pixels. <br>
     * This method requires page to be opened.
     */
    public int getPageWidth(int index) {
        Long pagePtr;
        if ((pagePtr = mNativePagesPtr.get(index)) != null) {
            return nativeGetPageWidthPixel(pagePtr, mCurrentDpi);
        }
        return 0;

    }

    /**
     * Get page height in pixels. <br>
     * This method requires page to be opened.
     */
    public int getPageHeight(int index) {
        Long pagePtr;
        if ((pagePtr = mNativePagesPtr.get(index)) != null) {
            return nativeGetPageHeightPixel(pagePtr, mCurrentDpi);
        }
        return 0;
    }

    /**
     * Get page width in PostScript points (1/72th of an inch).<br>
     * This method requires page to be opened.
     */
    public int getPageWidthPoint(int index) {
        Long pagePtr;
        if ((pagePtr = mNativePagesPtr.get(index)) != null) {
            return nativeGetPageWidthPoint(pagePtr);
        }
        return 0;
    }

    /**
     * Get page height in PostScript points (1/72th of an inch).<br>
     * This method requires page to be opened.
     */
    public int getPageHeightPoint(int index) {
        Long pagePtr;
        if ((pagePtr = mNativePagesPtr.get(index)) != null) {
            return nativeGetPageHeightPoint(pagePtr);
        }
        return 0;
    }

    /**
     * Get size of page in pixels.<br>
     * This method does not require given page to be opened.
     */
    public Size getPageSize(int index) {
        return nativeGetPageSizeByIndex(mNativeDocPtr, index, mCurrentDpi);
    }

    /**
     * Get the rotation of page<br>
     */
    public int getPageRotation(int index) {
        Long pagePtr;
        if ((pagePtr = mNativePagesPtr.get(index)) != null) {
            return nativeGetPageRotation(pagePtr);
        }
        return 0;
    }

    /**
     * Render page fragment on {@link Surface}.<br>
     * Page must be opened before rendering.
     */
    public void renderPage(Surface surface, int pageIndex,
                           int startX, int startY, int drawSizeX, int drawSizeY) {
        renderPage(surface, pageIndex, startX, startY, drawSizeX, drawSizeY, false);
    }

    /**
     * Render page fragment on {@link Surface}. This method allows to render annotations.<br>
     * Page must be opened before rendering.
     */
    public void renderPage(Surface surface, int pageIndex,
                           int startX, int startY, int drawSizeX, int drawSizeY,
                           boolean renderAnnot) {
        try {
            //nativeRenderPage(mNativePagesPtr.get(pageIndex), surface, mCurrentDpi);
            nativeRenderPage(mNativePagesPtr.get(pageIndex), surface, mCurrentDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot);
        } catch (NullPointerException e) {
            Log.e(TAG, "mContext may be null");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Exception throw from native");
            e.printStackTrace();
        }
    }

    /**
     * Render page fragment on {@link Bitmap}.<br>
     * Page must be opened before rendering.
     * <p>
     * Supported bitmap configurations:
     * <ul>
     * <li>ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
     * <li>RGB_565 - little worse quality, twice less memory usage
     * </ul>
     */
    public void renderPageBitmap(Bitmap bitmap, int pageIndex,
                                 int startX, int startY, int drawSizeX, int drawSizeY) {
        renderPageBitmap(bitmap, pageIndex, startX, startY, drawSizeX, drawSizeY, false);
    }

    /**
     * Render page fragment on {@link Bitmap}. This method allows to render annotations.<br>
     * Page must be opened before rendering.
     * <p>
     * For more info see {@link PdfiumSDK#renderPageBitmap(Bitmap, int, int, int, int, int)}
     */
    public void renderPageBitmap(Bitmap bitmap, int pageIndex,
                                 int startX, int startY, int drawSizeX, int drawSizeY,
                                 boolean renderAnnot) {
        try {
            nativeRenderPageBitmap(mNativePagesPtr.get(pageIndex), bitmap, mCurrentDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot);
        } catch (NullPointerException e) {
            Log.e(TAG, "mContext may be null");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Exception throw from native");
            e.printStackTrace();
        }
    }

    /**
     * Release native resources and opened file
     */
    public void closeDocument() {
        for (Integer index : mNativePagesPtr.keySet()) {
            nativeClosePage(mNativePagesPtr.get(index));
        }
        mNativePagesPtr.clear();

        for (Integer ptr : mNativeTextPagesPtr.keySet()) {
            nativeCloseTextPage(mNativeTextPagesPtr.get(ptr));
        }
        mNativeTextPagesPtr.clear();

        nativeCloseDocument(mNativeDocPtr);

        if (mFileDescriptor != null) {
            try {
                mFileDescriptor.close();
            } catch (IOException ignored) {
            } finally {
                mFileDescriptor = null;
            }
        }
    }

    /**
     * Get metadata for given document
     */
    public Meta getDocumentMeta() {
        return new Meta(
                nativeGetDocumentMetaText(mNativeDocPtr, "Title"),
                nativeGetDocumentMetaText(mNativeDocPtr, "Author"),
                nativeGetDocumentMetaText(mNativeDocPtr, "Subject"),
                nativeGetDocumentMetaText(mNativeDocPtr, "Keywords"),
                nativeGetDocumentMetaText(mNativeDocPtr, "Creator"),
                nativeGetDocumentMetaText(mNativeDocPtr, "Producer"),
                nativeGetDocumentMetaText(mNativeDocPtr, "CreationDate"),
                nativeGetDocumentMetaText(mNativeDocPtr, "ModDate")
        );
    }

    /**
     * Get table of contents (bookmarks) for given document
     */
    public List<Bookmark> getTableOfContents() {

        List<Bookmark> topLevel = new ArrayList<>();
        Long first = nativeGetFirstChildBookmark(mNativeDocPtr, null);
        if (first != null) {
            recursiveGetBookmark(topLevel, first);
        }
        return topLevel;
    }

    private void recursiveGetBookmark(List<Bookmark> tree, long bookmarkPtr) {
        Bookmark bookmark = new Bookmark(bookmarkPtr);

        bookmark.setTitle(nativeGetBookmarkTitle(bookmarkPtr));
        bookmark.setPageIdx(nativeGetBookmarkDestIndex(mNativeDocPtr, bookmarkPtr));
        tree.add(bookmark);

        Long child = nativeGetFirstChildBookmark(mNativeDocPtr, bookmarkPtr);
        if (child != null) {
            recursiveGetBookmark(bookmark.getChildren(), child);
        }

        Long sibling = nativeGetSiblingBookmark(mNativeDocPtr, bookmarkPtr);
        if (sibling != null) {
            recursiveGetBookmark(tree, sibling);
        }
    }

    /**
     * Get all links from given page
     */
    public List<Link> getPageLinks(int pageIndex) {
        List<Link> links = new ArrayList<>();
        Long nativePagePtr = mNativePagesPtr.get(pageIndex);
        if (nativePagePtr == null) {
            return links;
        }
        long[] linkPtrs = nativeGetPageLinks(nativePagePtr);
        for (long linkPtr : linkPtrs) {
            Integer index = nativeGetDestPageIndex(mNativeDocPtr, linkPtr);
            String uri = nativeGetLinkURI(mNativeDocPtr, linkPtr);

            RectF rect = nativeGetLinkRect(linkPtr);
            if (rect != null && (index != null || uri != null)) {
                links.add(new Link(rect, index, uri));
            }

        }
        return links;
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
     *                  2 (rotated 180 degrees), 3 (rotated 90 degrees counter-clockwise)
     * @param pageX     X value in page coordinates
     * @param pageY     Y value in page coordinate
     * @return mapped coordinates
     */
    public Point mapPageCoordsToDevice(int pageIndex, int startX, int startY, int sizeX,
                                       int sizeY, int rotate, double pageX, double pageY) {
        long pagePtr = mNativePagesPtr.get(pageIndex);
        return nativePageCoordinateToDevice(pagePtr, startX, startY, sizeX, sizeY, rotate, pageX, pageY);
    }

    /**
     * Convert the screen coordinates of a point to page coordinates.
     * <p>
     * The page coordinate system has its origin at the left-bottom corner
     * of the page, with the X-axis on the bottom going to the right, and
     * the Y-axis on the left side going up.
     * <p>
     * NOTE: this coordinate system can be altered when you zoom, scroll,
     * or rotate a page, however, a point on the page should always have
     * the same coordinate values in the page coordinate system.
     * <p>
     * The device coordinate system is device dependent. For screen device,
     * its origin is at the left-top corner of the window. However this
     * origin can be altered by the Windows coordinate transformation
     * utilities.
     * <p>
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
     *                  0 (normal)
     *                  1 (rotated 90 degrees clockwise)
     *                  2 (rotated 180 degrees)
     *                  3 (rotated 90 degrees counter-clockwise)
     * @param deviceX   X value in device coordinates to be converted.
     * @param deviceY   Y value in device coordinates to be converted.
     */
    public PointF mapDeviceCoordinateToPage(int pageIndex, int startX, int startY, int sizeX,
                                            int sizeY, int rotate, int deviceX, int deviceY) {
        long pagePtr = mNativePagesPtr.get(pageIndex);
        return nativeDeviceCoordinateToPage(pagePtr, startX, startY, sizeX, sizeY, rotate, deviceX, deviceY);
    }

    /**
     * @return mapped coordinates
     */
    public RectF mapPageCoordinateToDevice(int pageIndex, int startX, int startY, int sizeX,
                                           int sizeY, int rotate, RectF coords) {

        Point leftTop = mapPageCoordsToDevice(pageIndex, startX, startY, sizeX, sizeY, rotate,
                coords.left, coords.top);
        Point rightBottom = mapPageCoordsToDevice(pageIndex, startX, startY, sizeX, sizeY, rotate,
                coords.right, coords.bottom);
        return new RectF(leftTop.x, leftTop.y, rightBottom.x, rightBottom.y);
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
    public long prepareTextInfo(int pageIndex) {
        long textPagePtr;
        textPagePtr = nativeLoadTextPage(mNativeDocPtr, pageIndex);
        if (validPtr(textPagePtr)) {
            mNativeTextPagesPtr.put(pageIndex, textPagePtr);
        }
        return textPagePtr;
    }

    /**
     * Release all resources allocated for a text page information structure.
     *
     * @param pageIndex index of page.
     */
    public void releaseTextInfo(int pageIndex) {
        long textPagePtr;
        textPagePtr = mNativeTextPagesPtr.get(pageIndex);
        if (validPtr(textPagePtr)) {
            nativeCloseTextPage(textPagePtr);
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
    public long[] prepareTextInfo(int fromIndex, int toIndex) {
        long[] textPagesPtr;
        textPagesPtr = nativeLoadTextPages(mNativeDocPtr, fromIndex, toIndex);
        int pageIndex = fromIndex;
        for (long page : textPagesPtr) {
            if (pageIndex > toIndex) break;
            if (validPtr(page)) {
                mNativeTextPagesPtr.put(pageIndex, page);
            }
            pageIndex++;
        }

        return textPagesPtr;
    }

    /**
     * Release all resources allocated for a text page information structure.
     *
     * @param fromIndex start index of page.
     * @param toIndex   end index of page.
     */
    public void releaseTextInfo(int fromIndex, int toIndex) {
        long textPagesPtr;
        for (int i = fromIndex; i < toIndex + 1; i++) {
            textPagesPtr = mNativeTextPagesPtr.get(i);
            if (validPtr(textPagesPtr)) {
                nativeCloseTextPage(textPagesPtr);
            }
        }
    }

    private Long ensureTextPage(int pageIndex) {
        Long ptr = mNativeTextPagesPtr.get(pageIndex);
        if (!validPtr(ptr)) {
            return prepareTextInfo(pageIndex);
        }
        return ptr;
    }

    public int countCharactersOnPage(int pageIndex) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            return validPtr(ptr) ? nativeTextCountChars(ptr) : 0;
        } catch (Exception e) {
            return 0;
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
    public String extractCharacters(int pageIndex, int startIndex, int length) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            if (!validPtr(ptr)) {
                return null;
            }
            short[] buf = new short[length + 1];

            int r = nativeTextGetText(ptr, startIndex, length, buf);

            byte[] bytes = new byte[(r - 1) * 2];
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < r - 1; i++) {
                short s = buf[i];
                bb.putShort(s);
            }

            return new String(bytes, "UTF-16LE");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get Unicode of a character in a page.
     *
     * @param pageIndex index of page.
     * @param index     Zero-based index of the character.
     * @return The Unicode of the particular character. If a character is not encoded in Unicode, the return value will be zero.
     */
    public char extractCharacter(int pageIndex, int index) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            return validPtr(ptr) ? (char) nativeTextGetUnicode(ptr, index) : (char) 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get bounding box of a particular character.
     *
     * @param pageIndex index of page.
     * @param index     Zero-based index of the character.
     * @return the character position measured in PDF "user space".
     */
    public RectF measureCharacterBox(int pageIndex, int index) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            if (!validPtr(ptr)) {
                return null;
            }
            double[] o = nativeTextGetCharBox(ptr, index);
            RectF r = new RectF();
            r.left = (float) o[0];
            r.right = (float) o[1];
            r.bottom = (float) o[2];
            r.top = (float) o[3];
            return r;
        } catch (Exception e) {
            return null;
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
    public int getCharacterIndex(int pageIndex, double x, double y, double xTolerance, double yTolerance) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            return validPtr(ptr) ? nativeTextGetCharIndexAtPos(ptr, x, y, xTolerance, yTolerance) : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Count number of rectangular areas occupied by a segment of texts.
     * <p>
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
    public int countTextRect(int pageIndex, int charIndex, int count) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            return validPtr(ptr) ? nativeTextCountRects(ptr, charIndex, count) : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Get a rectangular area from the result generated by FPDFText_CountRects.
     *
     * @param pageIndex index of page.
     * @param rectIndex Zero-based index for the rectangle.
     * @return the text rectangle.
     */
    public RectF getTextRect(int pageIndex, int rectIndex) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            if (!validPtr(ptr)) {
                return null;
            }
            double[] o = nativeTextGetRect(ptr, rectIndex);
            RectF r = new RectF();
            r.left = (float) o[0];
            r.top = (float) o[1];
            r.right = (float) o[2];
            r.bottom = (float) o[3];
            return r;
        } catch (Exception e) {
            return null;
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
     * <p>
     * Generally you should pass a buffer at least one larger than this if you want a terminating NUL,
     * which will be provided if space is available. Otherwise, return number of characters copied
     * into the buffer, including the terminating NUL  when space for it is available.
     */
    public String extractText(int pageIndex, RectF rect) {
        try {
            Long ptr = ensureTextPage(pageIndex);
            if (!validPtr(ptr)) {
                return null;
            }

            int length = nativeTextGetBoundedTextLength(ptr, rect.left, rect.top, rect.right, rect.bottom);
            if (length <= 0) {
                return null;
            }

            short[] buf = new short[length + 1];

            int r = nativeTextGetBoundedText(ptr, rect.left, rect.top, rect.right, rect.bottom, buf);

            byte[] bytes = new byte[(r - 1) * 2];
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < r - 1; i++) {
                short s = buf[i];
                bb.putShort(s);
            }
            return new String(bytes, "UTF-16LE");
        } catch (Exception e) {
            return null;
        }
    }

    private boolean validPtr(Long ptr) {
        return ptr != null && ptr != -1;
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
    public TextSearchContext newPageSearch(int pageIndex, String query, boolean matchCase, boolean matchWholeWord) {
        return new FPDFTextSearchContext(pageIndex, query, matchCase, matchWholeWord) {

            private Long mSearchHandlePtr;

            @Override
            public void prepareSearch() {

                long textPage = prepareTextInfo(pageIndex);

                if (hasSearchHandle(pageIndex)) {
                    long sPtr = mNativeSearchHandlePtr.get(pageIndex);
                    nativeSearchStop(sPtr);
                }

                this.mSearchHandlePtr = nativeSearchStart(textPage, query, matchCase, matchWholeWord);
            }

            @Override
            public int countResult() {
                if (validPtr(mSearchHandlePtr)) {
                    return nativeCountSearchResult(mSearchHandlePtr);
                }
                return -1;
            }

            @Override
            public RectF searchNext() {
                if (validPtr(mSearchHandlePtr)) {
                    mHasNext = nativeSearchNext(mSearchHandlePtr);
                    if (mHasNext) {
                        int index = nativeGetCharIndexOfSearchResult(mSearchHandlePtr);
                        if (index > -1) {
                            return measureCharacterBox(this.getPageIndex(), index);
                        }
                    }
                }

                mHasNext = false;
                return null;
            }

            @Override
            public RectF searchPrev() {
                if (validPtr(mSearchHandlePtr)) {
                    mHasPrev = nativeSearchPrev(mSearchHandlePtr);
                    if (mHasPrev) {
                        int index = nativeGetCharIndexOfSearchResult(mSearchHandlePtr);
                        if (index > -1) {
                            return measureCharacterBox(this.getPageIndex(), index);
                        }
                    }
                }

                mHasPrev = false;
                return null;
            }

            @Override
            public void stopSearch() {
                super.stopSearch();
                if (validPtr(mSearchHandlePtr)) {
                    nativeSearchStop(mSearchHandlePtr);
                    mNativeSearchHandlePtr.remove(getPageIndex());
                }
            }
        };

    }

    public int getCurrentDpi() {
        return mCurrentDpi;
    }

    public void setCurrentDpi(int d) {
        mCurrentDpi = d;
    }

    public boolean hasPage(int index) {
        return mNativePagesPtr.containsKey(index);
    }

    public boolean hasTextPage(int index) {
        return mNativeTextPagesPtr.containsKey(index);
    }

    public boolean hasSearchHandle(int index) {
        return mNativeSearchHandlePtr.containsKey(index);
    }
}
