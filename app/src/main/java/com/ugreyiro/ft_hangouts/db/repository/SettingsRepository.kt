package com.ugreyiro.ft_hangouts.db.repository

import android.content.ContentValues
import com.ugreyiro.ft_hangouts.db.DbContracts
import com.ugreyiro.ft_hangouts.db.FtHangoutsDatabaseHelper
import com.ugreyiro.ft_hangouts.db.query.SelectQuery
import com.ugreyiro.ft_hangouts.exception.EntryNotFoundException

class SettingsRepository(dbHelper : FtHangoutsDatabaseHelper) : SqliteRepository(dbHelper) {

    companion object Queries {
        private val settingValueQuery = SelectQuery(
            tableName = DbContracts.SettingEntry.TABLE_NAME,
            columns = arrayOf(DbContracts.SettingEntry.COLUMN_SETTING_VALUE),
            selection = "${DbContracts.SettingEntry.COLUMN_SETTING_NAME} = ?"
        )
    }
    fun updateSetting(name : String, value : String) {
        dbHelper.writableDatabase.update(
            DbContracts.SettingEntry.TABLE_NAME,
            ContentValues().apply {
                put(DbContracts.SettingEntry.COLUMN_SETTING_VALUE, value)
            },
            "${DbContracts.SettingEntry.COLUMN_SETTING_NAME} = ?",
            arrayOf(name)
        )
    }
    fun settingValueByName(name : String) : Int {
        val cursor = executeSelectQuery(settingValueQuery, arrayOf(name))
        return cursor.use {
            if (it.moveToNext()) {
                it.getInt(it.getColumnIndexOrThrow(DbContracts.SettingEntry.COLUMN_SETTING_VALUE))
            } else {
                throw EntryNotFoundException("Setting $name not found")
            }
        }
    }
}