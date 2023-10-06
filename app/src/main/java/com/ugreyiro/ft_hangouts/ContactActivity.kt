package com.ugreyiro.ft_hangouts

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ugreyiro.ft_hangouts.db.ContactsDatabaseHelper
import com.ugreyiro.ft_hangouts.exception.PhoneNumberAlreadyExistsException
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.Gender

class ContactActivity : AppCompatActivity() {
    private lateinit var contactIdTextView : TextView
    private lateinit var phoneNumberEditText : EditText
    private lateinit var firstNameEditText : EditText
    private lateinit var lastNameEditText : EditText
    private lateinit var genderRadioGroup : RadioGroup
    private lateinit var commentEditText : EditText
    private lateinit var saveBtn : Button

    private val contactsDbHelper = ContactsDatabaseHelper(this)

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
        initSaveButton()

        fillFields()
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
        saveBtn.setOnClickListener{
            try {
                val contact = parseContact()
                if (contact != null) {
                    if (contact.id == null)
                        createContact(contact)
                    else
                        updateContact(contact)
                }
                runMainActivity()
            } catch (ex : PhoneNumberAlreadyExistsException) {
                Toast.makeText(
                    this,
                    getString(R.string.phone_number_already_exists),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun fillFields() {
        if (intent.hasExtra("id")) {
            contactIdTextView.text = intent.getLongExtra("id", 0).toString()
        }
        fillTextEditByExtra(phoneNumberEditText, intent, "phoneNumber")
        fillTextEditByExtra(firstNameEditText, intent, "firstName")
        fillTextEditByExtra(lastNameEditText, intent, "lastName")
        fillTextEditByExtra(commentEditText, intent, "comment")
        if (intent.hasExtra("gender")) {
            val gender = Gender.valueOf(intent.getStringExtra("gender")!!)
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
        val createdId = contactsDbHelper.create(contact)
        if (createdId < 0) {
            Toast.makeText(
                this,
                getString(R.string.contact_create_db_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateContact(contact: Contact) {
        contactsDbHelper.update(contact)
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
        if (firstNameEditText.text.isNullOrBlank()) {
            firstNameEditText.error = getString(R.string.field_required)
            failed = true
        }
        return !failed
    }

    private fun getCheckedGender() : Gender =
        Gender.valueOf(
            findViewById<RadioButton>(genderRadioGroup.checkedRadioButtonId)
                .text
                .toString().uppercase()
        )

    private fun runMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
            .also { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        startActivity(intent)
    }
}