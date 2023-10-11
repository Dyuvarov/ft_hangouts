package com.ugreyiro.ft_hangouts.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ugreyiro.ft_hangouts.R
import com.ugreyiro.ft_hangouts.db.FtHangoutsDatabaseHelper
import com.ugreyiro.ft_hangouts.db.repository.ContactsRepository
import com.ugreyiro.ft_hangouts.db.repository.SettingsRepository
import com.ugreyiro.ft_hangouts.exception.PhoneNumberAlreadyExistsException
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.Gender

const val CONTACT_ID_EXTRA = "id"
const val PHONE_NUMBER_EXTRA = "phoneNumber"
const val FIRST_NAME_EXTRA = "firstName"
const val LAST_NAME_EXTRA = "lastName"
const val COMMENT_EXTRA = "comment"
const val GENDER_EXTRA = "gender"

class ContactActivity : BaseActivity() {

    companion object {
        private const val MAX_PHONE_NUMBER_LENGTH = 20
        private const val MAX_NAME_LENGTH = 30
    }


    private lateinit var contactIdTextView : TextView
    private lateinit var phoneNumberEditText : EditText
    private lateinit var firstNameEditText : EditText
    private lateinit var lastNameEditText : EditText
    private lateinit var genderRadioGroup : RadioGroup
    private lateinit var commentEditText : EditText
    private lateinit var saveBtn : Button
    private lateinit var deleteBtn : Button
    private lateinit var messageBtn : Button

    /**
     * True when activity opened for creating new contact.
     * False if activity opened for existing contact
     */
    private var isNew = true

    private val dbHelper = FtHangoutsDatabaseHelper(this)
    private val contactsRepository = ContactsRepository(dbHelper)
    override val settingsRepository = SettingsRepository(dbHelper)

    private val genderRadioIdMap = mapOf(
        Gender.MALE to R.id.maleGenderRadioBtn,
        Gender.FEMALE to R.id.femaleGenderRadioBtn,
        Gender.OTHER to R.id.otherGenderRadioBtn,
        Gender.UNKNOWN to R.id.unknownGenderRadioBtn
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        initFields()
        fillFields()

        initSaveButton()
        initDeleteButton()
        initMessaging()
    }

    private fun initFields() {
        contactIdTextView = findViewById(R.id.contactIdTextView)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        commentEditText = findViewById(R.id.commentEditText)
    }

    private fun initSaveButton() {
        saveBtn = findViewById(R.id.saveButton)
        saveBtn.setOnClickListener { saveButtonOnClick() }
    }

    private fun initDeleteButton() {
        deleteBtn = findViewById(R.id.deleteButton)
        if (isNew) {
            deleteBtn.visibility = View.GONE
            return
        }
        deleteBtn.setOnClickListener { deleteButtonOnClick()  }
    }

    private fun initMessaging() {
        messageBtn = findViewById(R.id.msgButton)
        if (isNew) {
            messageBtn.visibility = View.GONE
        } else {
            messageBtn.setOnClickListener { messageButtonOnClick() }
        }
    }

    private fun fillFields() {
        if (intent.hasExtra(CONTACT_ID_EXTRA)) {
            contactIdTextView.text = intent.getLongExtra(CONTACT_ID_EXTRA, 0).toString()
            isNew = false
        }
        fillTextEditByExtra(phoneNumberEditText, intent, PHONE_NUMBER_EXTRA)
        fillTextEditByExtra(firstNameEditText, intent, FIRST_NAME_EXTRA)
        fillTextEditByExtra(lastNameEditText, intent, LAST_NAME_EXTRA)
        fillTextEditByExtra(commentEditText, intent, COMMENT_EXTRA)
        if (intent.hasExtra(GENDER_EXTRA)) {
            val gender = Gender.valueOf(intent.getStringExtra(GENDER_EXTRA)!!)
            val radioBtn = findViewById<RadioButton>(genderRadioIdMap[gender]!!)
            radioBtn.isChecked = true
        }
    }

    private fun fillTextEditByExtra(editText : EditText, intent : Intent, extraKey : String) {
        if (intent.hasExtra(extraKey)) {
            editText.setText(intent.getStringExtra(extraKey))
        }
    }

    /** Validate and save contact in DB */
    private fun createContact(contact: Contact) {
        val createdId = contactsRepository.create(contact)
        if (createdId < 0) {
            Toast.makeText(
                this,
                getString(R.string.contact_create_db_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateContact(contact: Contact) {
        contactsRepository.update(contact)
    }

    private fun deleteContact(contactId : Long) {
        contactsRepository.delete(contactId)
    }

    private fun parseContact() : Contact? {
        if (!validateFieldsBeforeSave()) return null
        val id = if (contactIdTextView.text.isNullOrBlank()) {
            null
        } else {
            contactIdTextView.text.toString().toLong()
        }
        return Contact(
            id = id,
            phoneNumber = phoneNumberEditText.text.toString(),
            firstName = firstNameEditText.text.toString(),
            lastName = lastNameEditText.text?.toString(),
            gender = getCheckedGender(),
            comment = commentEditText.text.toString()
        )
    }

    private fun validateFieldsBeforeSave() : Boolean {
        var failed = false;
        if (phoneNumberEditText.text.isNullOrBlank()) {
            phoneNumberEditText.error = getString(R.string.field_required)
            failed = true
        }
        if (phoneNumberEditText.text.length > MAX_PHONE_NUMBER_LENGTH) {
            phoneNumberEditText.error = "${getString(R.string.value_too_long)} $MAX_PHONE_NUMBER_LENGTH"
            failed = true
        }
        if (firstNameEditText.text.isNullOrBlank()) {
            firstNameEditText.error = getString(R.string.field_required)
            failed = true
        }
        if (firstNameEditText.text.length > MAX_NAME_LENGTH) {
            firstNameEditText.error = "${getString(R.string.value_too_long)} $MAX_NAME_LENGTH"
            failed = true
        }
        if (lastNameEditText.text.length > MAX_NAME_LENGTH) {
            lastNameEditText.error = "${getString(R.string.value_too_long)} $MAX_NAME_LENGTH"
            failed = true
        }
        return !failed
    }

    private fun getCheckedGender() : Gender =
        genderRadioIdMap
            .filterValues { it == genderRadioGroup.checkedRadioButtonId}.keys.first()


    private fun saveButtonOnClick() {
        try {
            val contact = parseContact()
            if (contact != null) {
                if (isNew)
                    createContact(contact)
                else
                    updateContact(contact)
                finish()
            }
        } catch (ex : PhoneNumberAlreadyExistsException) {
            Toast.makeText(
                this,
                getString(R.string.phone_number_already_exists),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun unsavedChanges() : Boolean {
        val phoneNumber = phoneNumberEditText.text.toString()
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text?.toString()
        val gender = getCheckedGender()
        val comment = commentEditText.text.toString()

        return phoneNumber != intent.getStringExtra(PHONE_NUMBER_EXTRA)
                || firstName != intent.getStringExtra(FIRST_NAME_EXTRA)
                || lastName != intent.getStringExtra(LAST_NAME_EXTRA)
                || gender.name != intent.getStringExtra(GENDER_EXTRA)
                || comment != intent.getStringExtra(COMMENT_EXTRA)
    }

    private fun deleteButtonOnClick() {
        val dialog = android.app.AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_contact_dialog))
            .setPositiveButton(getString(R.string.delete_contact_dialog_positive_btn)) { _, _ ->
                deleteContact(contactIdTextView.text.toString().toLong())
                finish()
            }
            .setNegativeButton(getString(R.string.delete_contact_dialog_negative_btn)) { _, _ -> }
            .create()
        dialog.show()
    }

    private fun messageButtonOnClick() {
        if (unsavedChanges()) {
            Toast.makeText(
                this,
                getString(R.string.unsaved_changes),
                Toast.LENGTH_LONG
            ).show()
        } else {
            val smsAllowed = getSmsPermissions()
            if (smsAllowed) {
                startChatActivity()
            }
        }
    }

    private fun startChatActivity() {
        val intent = Intent(this, ChatActivity::class.java).also {
            it.putExtra(PHONE_NUMBER_EXTRA, intent.getStringExtra(PHONE_NUMBER_EXTRA))
            it.putExtra(FIRST_NAME_EXTRA, intent.getStringExtra(FIRST_NAME_EXTRA))
        }
        startActivity(intent)
    }

    private fun getSmsPermissions() : Boolean{
        val notGranted = listOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS
        ).filterNot { hasPermission(it, this) }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 0)
        }
        return notGranted.isEmpty()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val smsAllowed = grantResults
            .filter { it == PackageManager.PERMISSION_GRANTED }
            .size == 3
        if (smsAllowed) {
            startChatActivity()
        }
    }
}