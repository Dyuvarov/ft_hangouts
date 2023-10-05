package com.ugreyiro.ft_hangouts.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.ugreyiro.ft_hangouts.db.ContactsContract.ContactEntry.COLUMN_COMMENT
import com.ugreyiro.ft_hangouts.db.ContactsContract.ContactEntry.COLUMN_FIRST_NAME
import com.ugreyiro.ft_hangouts.db.ContactsContract.ContactEntry.COLUMN_GENDER
import com.ugreyiro.ft_hangouts.db.ContactsContract.ContactEntry.COLUMN_LAST_NAME
import com.ugreyiro.ft_hangouts.db.ContactsContract.ContactEntry.COLUMN_PHONE_NUMBER
import com.ugreyiro.ft_hangouts.db.ContactsContract.ContactEntry.TABLE_NAME
import com.ugreyiro.ft_hangouts.db.ContactsContract.DATABASE_NAME
import com.ugreyiro.ft_hangouts.db.ContactsContract.DATABASE_VERSION
import com.ugreyiro.ft_hangouts.exception.PhoneNumberAlreadyExistsException
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.ContactListDto

private const val SQL_CREATE_CONTACTS_TABLE =
    """CREATE TABLE IF NOT EXISTS ${ContactsContract.ContactEntry.TABLE_NAME} (
           ${BaseColumns._ID} INTEGER PRIMARY KEY,
            $COLUMN_PHONE_NUMBER TEXT NOT NULL UNIQUE,
            $COLUMN_FIRST_NAME TEXT NOT NULL,
            $COLUMN_LAST_NAME TEXT,
            $COLUMN_GENDER TEXT NOT NULL,
            $COLUMN_COMMENT TEXT
            );
        """

private const val SQL_DROP_CONTACTS_TABLE =
    "DROP TABLE IF EXISTS ${ContactsContract.ContactEntry.TABLE_NAME}"
class ContactsDatabaseHelper(context : Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DROP_CONTACTS_TABLE)
        onCreate(db)
    }

    fun create(contact : Contact) : Long {
        if (phoneNumberAlreadyExists(contact.phoneNumber)) {
            throw PhoneNumberAlreadyExistsException()
        }
        return writableDatabase.insert(
            TABLE_NAME,
            null,
            contact.toContentValues()
        ).also { Log.i("INFO", "created contact with id = $it") }
    }


    fun findAllAsListDto() : List<ContactListDto> {
        val projection = arrayOf(
            BaseColumns._ID,
            COLUMN_FIRST_NAME,
            COLUMN_LAST_NAME,
            COLUMN_PHONE_NUMBER
        )
        val sortOrder = COLUMN_FIRST_NAME

        val cursor = readableDatabase.query(
            TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOrder
        )
        return cursor.use { mapCursorToContactListDtoList(cursor) }
    }

    fun phoneNumberAlreadyExists(phoneNumber : String) : Boolean {
        val cursor = readableDatabase.query(
            TABLE_NAME,
            arrayOf(BaseColumns._ID),
            "$COLUMN_PHONE_NUMBER = ?",
            arrayOf(phoneNumber),
            null,
            null,
            null
        )
        return cursor.use { it.moveToNext() }
    }

    private fun Contact.toContentValues() = ContentValues().apply {
        put(ContactsContract.ContactEntry.COLUMN_PHONE_NUMBER, phoneNumber)
        put(ContactsContract.ContactEntry.COLUMN_FIRST_NAME, firstName)
        put(ContactsContract.ContactEntry.COLUMN_LAST_NAME, lastName)
        put(ContactsContract.ContactEntry.COLUMN_GENDER, gender.name)
        put(ContactsContract.ContactEntry.COLUMN_COMMENT, comment)
    }

    private fun mapCursorToContactListDtoList(cursor : Cursor) : List<ContactListDto> {
        val result = mutableListOf<ContactListDto>()
        with (cursor) {
            while(moveToNext()) {
                result.add(
                    ContactListDto(
                        id = getLong(getColumnIndexOrThrow(BaseColumns._ID)),
                        firstName = getString(getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                        lastName = getString(getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                        phoneNumber = getString(getColumnIndexOrThrow(COLUMN_PHONE_NUMBER))
                    )
                )
            }
        }
        return result
    }
}