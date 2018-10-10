package org.benjinus.pdfium;

import android.graphics.RectF;

public class Link {
    private RectF bounds;
    private Integer destPageIdx;
    private String uri;

    public Link(RectF bounds, Integer destPageIdx, String uri) {
        this.bounds = bounds;
        this.destPageIdx = destPageIdx;
        this.uri = uri;
    }

    private Link() {
    }

    public Integer getDestPageIdx() {
        return destPageIdx;
    }

    public String getUri() {
        return uri;
    }

    public RectF getBounds() {
        return bounds;
    }
}