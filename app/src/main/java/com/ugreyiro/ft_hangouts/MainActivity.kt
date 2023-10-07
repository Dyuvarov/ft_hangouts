package com.ugreyiro.ft_hangouts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ugreyiro.ft_hangouts.adapter.ContactListAdapter
import com.ugreyiro.ft_hangouts.db.ContactsDatabaseHelper
import com.ugreyiro.ft_hangouts.exception.EntryNotFoundException

class MainActivity : AppCompatActivity() {
    private lateinit var contactsListView : ListView
    private lateinit var addContactBtn : ImageButton

    private val contactsDbHelper = ContactsDatabaseHelper(this)

    private val contactActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            fillContactsList()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initContactsListView()
        initAddContactButton()
    }

    private fun initContactsListView() {
        contactsListView = findViewById(R.id.contactsList)
        contactsListView.setOnItemClickListener { _, view, _, _ ->
            val contactId = view
                .findViewById<TextView>(R.id.contactIdTextView).text
                .toString()
                .toLong()
            openContactForm(contactId)
        }
        fillContactsList()
    }

    private fun fillContactsList() {
        val contacts = try {
            contactsDbHelper.findAllAsListDto()
        } catch (ex : Exception) {
            Log.e("ERROR", "CRITICAL: Cant extract contacts from database", ex)
            Toast.makeText(
                this,
                getString(R.string.cant_load_contacts_list),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        contactsListView.adapter = ContactListAdapter(this, contacts)
    }

    private fun initAddContactButton() {
        addContactBtn = findViewById(R.id.addContactBtn)
        addContactBtn.setOnClickListener {
            contactActivityLauncher.launch(Intent(this, ContactActivity::class.java))
        }
    }

    private fun openContactForm(contactId : Long) {
        val contact = try {
            contactsDbHelper.findContactById(contactId)
        } catch (ex : EntryNotFoundException) {
            Toast.makeText(
                this,
                getString(R.string.cant_open_contact_not_found),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val intent = Intent(this, ContactActivity::class.java).also {
            it.putExtra("id", contact.id)
            it.putExtra("phoneNumber", contact.phoneNumber)
            it.putExtra("firstName", contact.firstName)
            it.putExtra("lastName", contact.lastName)
            it.putExtra("gender", contact.gender.name)
            it.putExtra("comment", contact.comment)
        }
        contactActivityLauncher.launch(intent)
    }
}