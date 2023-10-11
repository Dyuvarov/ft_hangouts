package com.ugreyiro.ft_hangouts.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.ugreyiro.ft_hangouts.R
import com.ugreyiro.ft_hangouts.db.DbContracts.ContactEntry.COLUMN_COMMENT
import com.ugreyiro.ft_hangouts.db.DbContracts.ContactEntry.COLUMN_FIRST_NAME
import com.ugreyiro.ft_hangouts.db.DbContracts.ContactEntry.COLUMN_GENDER
import com.ugreyiro.ft_hangouts.db.DbContracts.ContactEntry.COLUMN_LAST_NAME
import com.ugreyiro.ft_hangouts.db.DbContracts.ContactEntry.COLUMN_PHONE_NUMBER
import com.ugreyiro.ft_hangouts.db.DbContracts.ContactEntry.TABLE_NAME
import com.ugreyiro.ft_hangouts.db.DbContracts.DATABASE_NAME
import com.ugreyiro.ft_hangouts.db.DbContracts.DATABASE_VERSION

private const val SQL_CREATE_CONTACTS_TABLE =
    """CREATE TABLE IF NOT EXISTS $TABLE_NAME (
           ${BaseColumns._ID} INTEGER PRIMARY KEY,
            $COLUMN_PHONE_NUMBER TEXT NOT NULL UNIQUE,
            $COLUMN_FIRST_NAME TEXT NOT NULL,
            $COLUMN_LAST_NAME TEXT,
            $COLUMN_GENDER TEXT NOT NULL,
            $COLUMN_COMMENT TEXT
            );
        """

private const val SQL_DROP_CONTACTS_TABLE =
    "DROP TABLE IF EXISTS $TABLE_NAME"

private const val SQL_CREATE_SETTINGS_TABLE =
    """CREATE TABLE IF NOT EXISTS ${DbContracts.SettingEntry.TABLE_NAME} (
        ${BaseColumns._ID} INTEGER PRIMARY KEY,
        ${DbContracts.SettingEntry.COLUMN_SETTING_NAME} TEXT NOT NULL,
        ${DbContracts.SettingEntry.COLUMN_SETTING_VALUE} TEXT NOT NULL
        );
    """

private const val SQL_DROP_SETTINGS_TABLE =
    "DROP TABLE IF EXISTS ${DbContracts.SettingEntry.TABLE_NAME}"

const val HEADER_COLOR_SETTING_NAME = "header_color"

class FtHangoutsDatabaseHelper(context : Context?)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_CONTACTS_TABLE)
        db?.execSQL(SQL_CREATE_SETTINGS_TABLE)
        setDefaultHeaderColorSetting(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DROP_CONTACTS_TABLE)
        db?.execSQL(SQL_DROP_SETTINGS_TABLE)
        onCreate(db)
    }
    private fun setDefaultHeaderColorSetting(db: SQLiteDatabase?) {
        db?.insert(
            DbContracts.SettingEntry.TABLE_NAME,
            null,
            ContentValues().apply {
                put(DbContracts.SettingEntry.COLUMN_SETTING_NAME, HEADER_COLOR_SETTING_NAME)
                put(DbContracts.SettingEntry.COLUMN_SETTING_VALUE, R.color.black)
            }
        )
    }
}