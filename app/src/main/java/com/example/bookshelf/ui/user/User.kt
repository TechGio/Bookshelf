package com.example.bookshelf.ui.user

class User {

    var nickname: String? = null
    var email: String? = null
    var uid: String? = null

    constructor(){}

    constructor(nickName: String?){
        this.nickname = nickName
    }

    constructor(nickName: String?, uid: String?){
        this.nickname = nickName
        this.uid = uid
    }

    constructor(nickName: String?, email: String?, uid: String?){
        this.nickname = nickName
        this.email = email
        this.uid = uid
    }
}