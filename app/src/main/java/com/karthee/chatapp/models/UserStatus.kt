package com.karthee.chatapp.models

data class UserStatus (val status: String="offline",val last_seen: Long=0,
                       val typing_status: String="non_typing",val chatuser: String?=null) {
}