package com.example.askai

class Message(var message: String, var sentBy: String) {


    companion object{
         val SENT_BY_ME = "me"
        const val SENT_BY_AI = "ai"
        const val SENT_BY_AI_IMAGE = "ai_image"

    }

}