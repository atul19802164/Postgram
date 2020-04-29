package com.tathagat.postgram

public class  notification {
    var user:String
    var icon:Int
    var body:String
    var title:String
    var  sented:String

    constructor(user:String, icon:Int, body:String, title:String, sented:String) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sented = sented;
    }}