////////////////////////////////////////////////////////////////////////////////
// Copyright © 2015-2018 NTKO. All rights reserved.
//
// THIS SOURCE CODE AND ANY ACCOMPANYING DOCUMENTATION ARE PROTECTED BY INTERNATIONAL COPYRIGHT LAW
// AND MAY NOT BE RESOLD OR REDISTRIBUTED. USAGE IS BOUND TO THE NTKO PRODUCTS LICENSE AGREEMENT.
// UNAUTHORIZED REPRODUCTION OR DISTRIBUTION IS SUBJECT TO CIVIL AND CRIMINAL PENALTIES.
// This notice may not be removed from this file.
////////////////////////////////////////////////////////////////////////////////

package org.benjinus.pdfium.util;

public class Size {
    private final int width;
    private final int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
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
        if (obj instanceof Size) {
            Size other = (Size) obj;
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
        // assuming most sizes are <2^16, doing a rotate will give us perfect hashing
        return height ^ ((width << (Integer.SIZE / 2)) | (width >>> (Integer.SIZE / 2)));
    }
}
