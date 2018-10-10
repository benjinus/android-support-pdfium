package org.benjinus.pdfium;

import java.util.ArrayList;
import java.util.List;

public class Bookmark {
    private String title;
    private long pageIdx;
    private List<Bookmark> children = new ArrayList<>();
    private final long mNativePtr;

    private Bookmark() {
        this(-1);
    }

    public Bookmark(long mNativePtr) {
        this.mNativePtr = mNativePtr;
    }

    public long getNativePtr() {
        return mNativePtr;
    }

    public List<Bookmark> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public String getTitle() {
        return title;
    }

    public long getPageIdx() {
        return pageIdx;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPageIdx(long pageIdx) {
        this.pageIdx = pageIdx;
    }

    public void setChildren(List<Bookmark> children) {
        this.children = children;
    }
}