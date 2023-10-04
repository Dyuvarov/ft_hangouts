package com.ugreyiro.ft_hangouts.model

data class Contact(
    val id : Long,
    val phoneNumber : String,
    val firstName : String,
    val lastName : String? = null,
    val gender : Gender = Gender.UNKNOWN,
    val comment : String? = null
)

fun Contact.fullName() : String = listOfNotNull(firstName, lastName).joinToString(separator = " ")