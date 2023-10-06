package com.ugreyiro.ft_hangouts.db.query

data class SelectQuery(
    val tableName : String,
    val columns : Array<String>,
    val selection : String? = null,
    val selectionArgs : Array<String>? = null,
    val groupBy : String? = null,
    val having : String? = null,
    val orderBy : String? = null
)