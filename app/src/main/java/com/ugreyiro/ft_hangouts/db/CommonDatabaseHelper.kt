package com.ugreyiro.ft_hangouts.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.ugreyiro.ft_hangouts.db.query.SelectQuery

abstract class CommonDatabaseHelper(
    context : Context,
    dbName : String,
    factory: CursorFactory?,
    version : Int
) : SQLiteOpenHelper(context, dbName, factory, version) {

}