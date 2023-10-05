package com.ugreyiro.ft_hangouts

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ugreyiro.ft_hangouts.db.ContactsDatabaseHelper
import com.ugreyiro.ft_hangouts.exception.PhoneNumberAlreadyExistsException
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.Gender

class ContactActivity : AppCompatActivity() {
    private lateinit var phoneNumberEditText : EditText
    private lateinit var firstNameEditText : EditText
    private lateinit var lastNameEditText : EditText
    private lateinit var genderRadioGroup : RadioGroup
    private lateinit var commentEditText : EditText
    private lateinit var saveBtn : Button

    private val contactsDbHelper = ContactsDatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        initFields()
        initSaveButton()
    }

    private fun initFields() {
        phoneNumberEditText = findViewById(R.id.editTxtPhoneNumber)
        firstNameEditText = findViewById(R.id.editTxtFirstName)
        lastNameEditText = findViewById(R.id.editTxtLastName)
        genderRadioGroup = findViewById(R.id.radioGroupGender)
        commentEditText = findViewById(R.id.editTxtComment)
    }

    private fun initSaveButton() {
        saveBtn = findViewById(R.id.buttonSave)
        saveBtn.setOnClickListener{
            try {
                createContact()
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

    /** Validate and save contact in DB */
    private fun createContact() {
        val contact = parseContact() ?: return
        val createdId = contactsDbHelper.create(contact)
        if (createdId < 0) {
            Toast.makeText(
                this,
                getString(R.string.contact_create_db_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun parseContact() : Contact? {
        if (!validateFieldsBeforeSave()) return null

        return Contact(
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