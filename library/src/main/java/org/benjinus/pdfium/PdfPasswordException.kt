package org.benjinus.pdfium

import java.lang.RuntimeException

class PdfPasswordException : RuntimeException {
    constructor() {}
    constructor(message: String?) : super(message) {}
}