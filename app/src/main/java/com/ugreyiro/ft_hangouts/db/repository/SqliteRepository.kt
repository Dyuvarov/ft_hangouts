package com.ugreyiro.ft_hangouts.db.repository

import android.database.Cursor
import com.ugreyiro.ft_hangouts.db.FtHangoutsDatabaseHelper
import com.ugreyiro.ft_hangouts.db.query.SelectQuery

open class SqliteRepository(
    protected val dbHelper : FtHangoutsDatabaseHelper
) {
    fun executeSelectQuery(query : SelectQuery, selectArgs : Array<String>? = null): Cursor =
        dbHelper.readableDatabase.query(
            query.tableName,
            query.columns,
            query.selection,
            selectArgs ?: query.selectionArgs,
            query.groupBy,
            query.having,
            query.orderBy
        )
}