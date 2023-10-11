package com.ugreyiro.ft_hangouts.model

data class Message(
    val date : Long,
    val body : String,
    val type : MessageType,
    val address : String
)