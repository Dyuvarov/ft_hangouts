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
import com.ugreyiro.ft_hangouts.db.query.SelectQuery
import com.ugreyiro.ft_hangouts.exception.EntryNotFoundException
import com.ugreyiro.ft_hangouts.exception.PhoneNumberAlreadyExistsException
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.ContactListDto
import com.ugreyiro.ft_hangouts.model.Gender

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
    "DROP TABLE IF EXISTS $TABLE_NAME"
class ContactsDatabaseHelper(context : Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object Queries {
        private val findAllAsListDtoQuery = SelectQuery(
            tableName = TABLE_NAME,
            columns = arrayOf(
                BaseColumns._ID,
                COLUMN_FIRST_NAME,
                COLUMN_LAST_NAME,
                COLUMN_PHONE_NUMBER
            ),
            orderBy = COLUMN_FIRST_NAME
        )

        private val phoneNumberAlreadyExistsQuery = SelectQuery(
            tableName = TABLE_NAME,
            columns = arrayOf(BaseColumns._ID),
            selection = "$COLUMN_PHONE_NUMBER = ?",
        )

        private val findContactByIdQuery = SelectQuery(
            tableName = TABLE_NAME,
            columns = arrayOf(
                BaseColumns._ID,
                COLUMN_FIRST_NAME,
                COLUMN_LAST_NAME,
                COLUMN_PHONE_NUMBER,
                COLUMN_GENDER,
                COLUMN_COMMENT
            ),
            selection = "${BaseColumns._ID} = ?"
        )
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DROP_CONTACTS_TABLE)
        onCreate(db)
    }

    fun create(contact : Contact) : Long {
        validatePhoneNumber(contact)
        return writableDatabase.insert(
            TABLE_NAME,
            null,
            contact.toContentValues()
        ).also { Log.i("INFO", "created contact with id = $it") }
    }

    fun update(contact : Contact) : Int {
        validatePhoneNumber(contact)
        return writableDatabase.update(
            TABLE_NAME,
            contact.toContentValues(),
            "${BaseColumns._ID} = ?",
            arrayOf(contact.id.toString())
        )
    }

    fun delete(contactId: Long) : Int {
        return writableDatabase.delete(
            TABLE_NAME,
            "${BaseColumns._ID} = ?",
            arrayOf(contactId.toString())
        )
    }

    fun findAllAsListDto() : List<ContactListDto> {
        val cursor = executeSelectQuery(findAllAsListDtoQuery)
        return cursor.use { mapCursorToContactListDtoList(it) }
    }

    fun phoneNumberAlreadyExists(phoneNumber : String, contact : Contact) : Boolean {
        val cursor = executeSelectQuery(phoneNumberAlreadyExistsQuery, arrayOf(phoneNumber))
        val foundId =  cursor.use {
            if (it.moveToNext() ) it.getLong(it.getColumnIndexOrThrow(BaseColumns._ID))
            else null
        }
        return if (foundId != null) {
            contact.id == null || foundId != contact.id
        } else {
            false
        }
    }

    fun findContactById(id : Long) : Contact {
        val cursor = executeSelectQuery(findContactByIdQuery, arrayOf(id.toString()))
        return cursor.use { mapCursorToContact(it) }
    }

    private fun validatePhoneNumber(contact: Contact) {
        if (phoneNumberAlreadyExists(contact.phoneNumber, contact)) {
            throw PhoneNumberAlreadyExistsException()
        }
    }

    private fun Contact.toContentValues() = ContentValues().apply {
        if ( this@toContentValues.id != null ) {
            put(BaseColumns._ID, this@toContentValues.id)
        }
        put(COLUMN_PHONE_NUMBER, phoneNumber)
        put(COLUMN_FIRST_NAME, firstName)
        put(COLUMN_LAST_NAME, lastName)
        put(COLUMN_GENDER, gender.name)
        put(COLUMN_COMMENT, comment)
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

    private fun mapCursorToContact(cursor: Cursor) : Contact =
        with(cursor) {
            if (moveToNext()) {
                Contact(
                    id = getLong(getColumnIndexOrThrow(BaseColumns._ID)),
                    firstName = getString(getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                    lastName = getString(getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                    phoneNumber = getString(getColumnIndexOrThrow(COLUMN_PHONE_NUMBER)),
                    gender = Gender.valueOf(getString(getColumnIndexOrThrow(COLUMN_GENDER))),
                    comment = getString(getColumnIndexOrThrow(COLUMN_COMMENT))
                )
            } else {
                throw EntryNotFoundException("Contact not found")
            }
        }

    private fun executeSelectQuery(query : SelectQuery, selectArgs : Array<String>? = null) =
        readableDatabase.query(
            query.tableName,
            query.columns,
            query.selection,
            selectArgs ?: query.selectionArgs,
            query.groupBy,
            query.having,
            query.orderBy
        )
}