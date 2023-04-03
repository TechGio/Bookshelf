package com.example.bookshelf

interface Communicator {

    fun passSearchBook(
        title: String?,
        subTitle: String?,
        authors: String?,
        publisher: String?,
        publishedDate: String?,
        description: String?,
        pageCount: String?,
        thumbnail: String?,
        shelf: String?,
        bid: String?,
        stars: String?,
        readDate: String?
    )

    fun passReadBook(
        title: String?,
        subTitle: String?,
        authors: String?,
        publisher: String?,
        publishedDate: String?,
        description: String?,
        pageCount: String?,
        thumbnail: String?,
        shelf: String?,
        bid: String?,
        stars: String?,
        readDate: String?
    )

    fun passNickname(nickname: String?)
}