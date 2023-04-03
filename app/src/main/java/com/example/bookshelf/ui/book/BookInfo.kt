package com.example.bookshelf.ui.book

import java.time.LocalDateTime

class BookInfo {

    private var title: String? = null
    private var subtitle: String? = null
    private var authors: String? = null
    private var publisher: String? = null
    private var publishedDate: String? = null
    private var description: String? = null
    private var pageCount: String? = null
    private var thumbnail: String? = null
    private var shelf: String? = null
    private var uid: String? = null
    private var bid: String? = null
    private var stars: String? = null
    private var readDate: String? = null

    constructor(){}

    constructor (
    title: String?, subtitle: String?, authors: String?, publisher: String?,
    publishedDate: String?, description: String?, pageCount: String?, thumbnail: String?, bid: String?, shelf: String?, stars: String?, uid: String?, readDate: String?) {
        this.title = title
        this.subtitle = subtitle
        this.authors = authors
        this.publisher = publisher
        this.publishedDate = publishedDate
        this.description = description
        this.pageCount = pageCount
        this.thumbnail = thumbnail
        this.bid = bid
        this.uid = uid
        this.shelf = shelf
        this.stars = stars
        this.readDate = readDate
    }

    fun getReadDate(): String?{
        return readDate
    }

    fun setReadDate(readDate: String){
        this.readDate = readDate
    }

    fun getStars(): String? {
        return stars
    }

    fun setStars(stars: String?) {
        this.stars = stars
    }

    fun getBid(): String? {
        return bid
    }

    fun setBid(bid: String?) {
        this.bid = bid
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }
    fun getShelf(): String? {
        return shelf
    }

    fun setShelf(shelf: String?) {
        this.shelf = shelf
    }
    fun getUid(): String? {
        return uid
    }

    fun setUid(uid: String?) {
        this.uid = uid
    }

    fun getSubtitle(): String? {
        return subtitle
    }

    fun setSubtitle(subtitle: String?) {
        this.subtitle = subtitle
    }

    fun getAuthors(): String? {
        return authors
    }

    fun setAuthors(authors: String?) {
        this.authors = authors
    }

    fun getPublisher(): String? {
        return publisher
    }

    fun setPublisher(publisher: String?) {
        this.publisher = publisher
    }

    fun getPublishedDate(): String? {
        return publishedDate
    }

    fun setPublishedDate(publishedDate: String?) {
        this.publishedDate = publishedDate
    }

    fun getDescription(): String? {
        return description
    }

    fun setDescription(description: String?) {
        this.description = description
    }

    fun getPageCount(): String? {
        return pageCount
    }

    fun setPageCount(pageCount: String) {
        this.pageCount = pageCount
    }

    fun getThumbnail(): String? {
        return thumbnail
    }

    fun setThumbnail(thumbnail: String?) {
        this.thumbnail = thumbnail
    }
}