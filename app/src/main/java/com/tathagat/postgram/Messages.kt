package com.tathagat.postgram

class Messages {
    var from:String
    var type:String
    var text:String
    var date:String
    var time:String
    var isseen:String
    var to:String
    var messageid:String
    var story_id:String
    var story_position:String
    constructor(from:String,to:String,messageid:String,type:String,text:String,date:String,time:String,isseen:String,story_id:String,story_position:String){
        this.from=from
        this.type=type
        this.text=text
        this.date=date
        this.time=time
        this.isseen=isseen
        this.to=to
        this.messageid=messageid
        this.story_id=story_id
        this.story_position=story_position
    }
}