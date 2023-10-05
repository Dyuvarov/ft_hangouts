package com.ugreyiro.ft_hangouts.model

data class Contact(
    var id : Long? = null,
    val phoneNumber : String,
    val firstName : String,
    val lastName : String? = null,
    val gender : Gender = Gender.UNKNOWN,
    val comment : String? = null
)