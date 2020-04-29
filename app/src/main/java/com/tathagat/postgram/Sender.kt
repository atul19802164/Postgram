package com.tathagat.postgram


public class Sender {
    var data: notification
var to:String

    constructor(data: notification, to:String) {
        this.data = data;
        this.to = to;
    }
}