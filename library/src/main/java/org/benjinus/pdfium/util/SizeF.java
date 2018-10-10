////////////////////////////////////////////////////////////////////////////////
// Copyright © 2015-2018 NTKO. All rights reserved.
//
// THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
// AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE NTKO PRODUCTS LICENSE AGREEMENT.
// UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
// This notice may not be removed from this file.
////////////////////////////////////////////////////////////////////////////////

package org.benjinus.pdfium.util;

public class SizeF {
    private final float width;
    private final float height;

    public SizeF(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof SizeF) {
            final SizeF other = (SizeF) obj;
            return width == other.width && height == other.height;
        }
        return false;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(width) ^ Float.floatToIntBits(height);
    }

    public Size toSize() {
        return new Size((int) width, (int) height);
    }
}
