package com.ugreyiro.ft_hangouts.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.ugreyiro.ft_hangouts.db.ContactsContract.DATABASE_NAME
import com.ugreyiro.ft_hangouts.db.ContactsContract.DATABASE_VERSION

private const val SQL_CREATE_CONTACTS_TABLE =
    """CREATE TABLE ${ContactsContract.ContactEntry.TABLE_NAME} (
           ${BaseColumns._ID} INTEGER PRIMARY KEY,
            ${ContactsContract.ContactEntry.COLUMN_PHONE_NUMBER} TEXT,
            ${ContactsContract.ContactEntry.COLUMN_FIRST_NAME} TEXT,
            ${ContactsContract.ContactEntry.COLUMN_LAST_NAME} TEXT,
            ${ContactsContract.ContactEntry.COLUMN_GENDER} TEXT,
            ${ContactsContract.ContactEntry.COLUMN_COMMENT} TEXT
        """

private const val SQL_DROP_CONTACTS_TABLE =
    "DROP TABLE IF EXISTS ${ContactsContract.ContactEntry.TABLE_NAME}"
class DatabaseHelper(context : Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_CONTACTS_TABLE)
        onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DROP_CONTACTS_TABLE)
        onCreate(db)
    }
}