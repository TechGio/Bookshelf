package com.example.bookshelf.ui.user

class AnnualReadingChallenge {

    private var num: String? = null
    private var year: String? = null

    constructor(){}

    constructor(num: String?, year: String?){
        this.num = num
        this.year = year
    }

    fun getNum(): String?{
        return num
    }

    fun getYear(): String?{
        return year
    }
}