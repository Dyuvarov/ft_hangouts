package com.ugreyiro.ft_hangouts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ugreyiro.ft_hangouts.adapter.ContactListAdapter
import com.ugreyiro.ft_hangouts.db.ContactsDatabaseHelper
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.Gender
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var contactsListView : ListView
    private lateinit var addContactBtn : ImageButton

    private val contactsDbHelper = ContactsDatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initContactsListView()
        initAddContactButton()
    }

    private fun initContactsListView() {
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
        contactsListView = findViewById(R.id.contactsList)
        contactsListView.adapter = ContactListAdapter(this, contacts)
    }

    private fun initAddContactButton() {
        addContactBtn = findViewById(R.id.addContactBtn)
        addContactBtn.setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
        }
    }
}