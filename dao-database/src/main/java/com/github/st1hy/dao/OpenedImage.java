package com.github.st1hy.dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "OPENED_IMAGE".
 */
public class OpenedImage {

    private Long id;
    /** Not-null value. */
    private String uri;
    /** Not-null value. */
    private java.util.Date date;

    public OpenedImage() {
    }

    public OpenedImage(Long id) {
        this.id = id;
    }

    public OpenedImage(Long id, String uri, java.util.Date date) {
        this.id = id;
        this.uri = uri;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getUri() {
        return uri;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /** Not-null value. */
    public java.util.Date getDate() {
        return date;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setDate(java.util.Date date) {
        this.date = date;
    }

}