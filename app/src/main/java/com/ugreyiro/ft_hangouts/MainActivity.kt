package com.ugreyiro.ft_hangouts

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.ugreyiro.ft_hangouts.adapter.ContactListAdapter
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.Gender

class MainActivity : AppCompatActivity() {

    private val contacts = listOf(
        Contact(id = 0, phoneNumber = "+33124124124", firstName = "John", lastName = "Holland"),
        Contact(id = 1, phoneNumber = "+781241415", firstName = "Alan", gender = Gender.MALE, comment = "Good guy"),
        Contact(id = 2, phoneNumber = "815812512512", firstName = "Tanya", lastName = "Lastname", gender = Gender.FEMALE)
    )

    private lateinit var contactsListView : ListView
    private lateinit var addContactBtn : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initContactsListView()
        initAddContactButton()
    }

    private fun initContactsListView() {
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