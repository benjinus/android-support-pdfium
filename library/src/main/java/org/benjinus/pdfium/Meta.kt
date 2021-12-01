package org.benjinus.pdfium

class Meta {
    var title: String? = null
        private set
    var author: String? = null
        private set
    var subject: String? = null
        private set
    var keywords: String? = null
        private set
    var creator: String? = null
        private set
    var producer: String? = null
        private set
    var creationDate: String? = null
        private set
    var modDate: String? = null
        private set

    constructor(
        title: String?,
        author: String?,
        subject: String?,
        keywords: String?,
        creator: String?,
        producer: String?,
        creationDate: String?,
        modDate: String?
    ) {
        this.title = title
        this.author = author
        this.subject = subject
        this.keywords = keywords
        this.creator = creator
        this.producer = producer
        this.creationDate = creationDate
        this.modDate = modDate
    }

    private constructor() {}

    override fun toString(): String {
        return "Meta{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", subject='" + subject + '\'' +
                ", keywords='" + keywords + '\'' +
                ", creator='" + creator + '\'' +
                ", producer='" + producer + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", modDate='" + modDate + '\'' +
                '}'
    }
}