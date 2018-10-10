package org.benjinus.pdfium.search;

import android.graphics.RectF;

public interface TextSearchContext {

    void prepareSearch();

    int getPageIndex();

    String getQuery();

    boolean isMatchCase();

    boolean isMatchWholeWord();

    int countResult();

    boolean hasNext();

    boolean hasPrev();

    RectF searchNext();

    RectF searchPrev();

    void startSearch();

    void stopSearch();
}