package org.benjinus.pdfium;

public class Meta {
    private String title;
    private String author;
    private String subject;
    private String keywords;
    private String creator;
    private String producer;
    private String creationDate;
    private String modDate;

    public Meta(String title, String author, String subject, String keywords, String creator, String producer, String creationDate, String modDate) {
        this.title = title;
        this.author = author;
        this.subject = subject;
        this.keywords = keywords;
        this.creator = creator;
        this.producer = producer;
        this.creationDate = creationDate;
        this.modDate = modDate;
    }

    private Meta() {
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubject() {
        return subject;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getCreator() {
        return creator;
    }

    public String getProducer() {
        return producer;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getModDate() {
        return modDate;
    }

    @Override
    public String toString() {
        return "Meta{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", subject='" + subject + '\'' +
                ", keywords='" + keywords + '\'' +
                ", creator='" + creator + '\'' +
                ", producer='" + producer + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", modDate='" + modDate + '\'' +
                '}';
    }
}