package com.ugreyiro.ft_hangouts.db.repository

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import android.util.Log
import com.ugreyiro.ft_hangouts.db.DbContracts
import com.ugreyiro.ft_hangouts.db.FtHangoutsDatabaseHelper
import com.ugreyiro.ft_hangouts.db.query.SelectQuery
import com.ugreyiro.ft_hangouts.exception.EntryNotFoundException
import com.ugreyiro.ft_hangouts.exception.PhoneNumberAlreadyExistsException
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.ContactListDto
import com.ugreyiro.ft_hangouts.model.Gender

class ContactsRepository(dbHelper : FtHangoutsDatabaseHelper) : SqliteRepository(dbHelper) {

    companion object Queries {
        private val findAllAsListDtoQuery = SelectQuery(
            tableName = DbContracts.ContactEntry.TABLE_NAME,
            columns = arrayOf(
                BaseColumns._ID,
                DbContracts.ContactEntry.COLUMN_FIRST_NAME,
                DbContracts.ContactEntry.COLUMN_LAST_NAME,
                DbContracts.ContactEntry.COLUMN_PHONE_NUMBER
            ),
            orderBy = DbContracts.ContactEntry.COLUMN_FIRST_NAME
        )

        private val phoneNumberAlreadyExistsQuery = SelectQuery(
            tableName = DbContracts.ContactEntry.TABLE_NAME,
            columns = arrayOf(BaseColumns._ID),
            selection = "${DbContracts.ContactEntry.COLUMN_PHONE_NUMBER} = ?",
        )

        private val findContactByIdQuery = SelectQuery(
            tableName = DbContracts.ContactEntry.TABLE_NAME,
            columns = arrayOf(
                BaseColumns._ID,
                DbContracts.ContactEntry.COLUMN_FIRST_NAME,
                DbContracts.ContactEntry.COLUMN_LAST_NAME,
                DbContracts.ContactEntry.COLUMN_PHONE_NUMBER,
                DbContracts.ContactEntry.COLUMN_GENDER,
                DbContracts.ContactEntry.COLUMN_COMMENT
            ),
            selection = "${BaseColumns._ID} = ?"
        )
    }

    fun create(contact : Contact) : Long {
        validatePhoneNumber(contact)
        return dbHelper.writableDatabase.insert(
            DbContracts.ContactEntry.TABLE_NAME,
            null,
            contact.toContentValues()
        ).also { Log.i("INFO", "created contact with id = $it") }
    }

    fun update(contact : Contact) : Int {
        validatePhoneNumber(contact)
        return dbHelper.writableDatabase.update(
            DbContracts.ContactEntry.TABLE_NAME,
            contact.toContentValues(),
            "${BaseColumns._ID} = ?",
            arrayOf(contact.id.toString())
        )
    }

    fun delete(contactId: Long) : Int {
        return dbHelper.writableDatabase.delete(
            DbContracts.ContactEntry.TABLE_NAME,
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
        put(DbContracts.ContactEntry.COLUMN_PHONE_NUMBER, phoneNumber)
        put(DbContracts.ContactEntry.COLUMN_FIRST_NAME, firstName)
        put(DbContracts.ContactEntry.COLUMN_LAST_NAME, lastName)
        put(DbContracts.ContactEntry.COLUMN_GENDER, gender.name)
        put(DbContracts.ContactEntry.COLUMN_COMMENT, comment)
    }

    private fun mapCursorToContactListDtoList(cursor : Cursor) : List<ContactListDto> {
        val result = mutableListOf<ContactListDto>()
        with (cursor) {
            while(moveToNext()) {
                result.add(
                    ContactListDto(
                        id = getLong(getColumnIndexOrThrow(BaseColumns._ID)),
                        firstName = getString(getColumnIndexOrThrow(DbContracts.ContactEntry.COLUMN_FIRST_NAME)),
                        lastName = getString(getColumnIndexOrThrow(DbContracts.ContactEntry.COLUMN_LAST_NAME)),
                        phoneNumber = getString(getColumnIndexOrThrow(DbContracts.ContactEntry.COLUMN_PHONE_NUMBER))
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
                    firstName = getString(getColumnIndexOrThrow(DbContracts.ContactEntry.COLUMN_FIRST_NAME)),
                    lastName = getString(getColumnIndexOrThrow(DbContracts.ContactEntry.COLUMN_LAST_NAME)),
                    phoneNumber = getString(getColumnIndexOrThrow(DbContracts.ContactEntry.COLUMN_PHONE_NUMBER)),
                    gender = Gender.valueOf(getString(getColumnIndexOrThrow(DbContracts.ContactEntry.COLUMN_GENDER))),
                    comment = getString(getColumnIndexOrThrow(DbContracts.ContactEntry.COLUMN_COMMENT))
                )
            } else {
                throw EntryNotFoundException("Contact not found")
            }
        }
}