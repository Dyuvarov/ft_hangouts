package com.ugreyiro.ft_hangouts.db

import android.provider.BaseColumns



object DbContracts {

    const val DATABASE_NAME = "ft_hangouts.db"
    const val DATABASE_VERSION = 4

    object ContactEntry : BaseColumns {
        const val TABLE_NAME = "contacts"
        const val COLUMN_FIRST_NAME = "first_name"
        const val COLUMN_LAST_NAME = "last_name"
        const val COLUMN_PHONE_NUMBER = "phone_number"
        const val COLUMN_GENDER = "gender"
        const val COLUMN_COMMENT = "comment"
    }

    object SettingEntry : BaseColumns {
        const val TABLE_NAME = "settings"
        const val COLUMN_SETTING_NAME = "setting_name"
        const val COLUMN_SETTING_VALUE = "setting_value"
    }
}