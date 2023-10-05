package com.ugreyiro.ft_hangouts.model

/** Short representation of contact to show in list of contacts */
class ContactListDto(
    val id : Long,
    val firstName : String,
    val lastName : String?,
    val phoneNumber : String
)
fun ContactListDto.fullName() : String =
    listOfNotNull(firstName, lastName).joinToString(separator = " ")