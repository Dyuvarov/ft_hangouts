package com.ugreyiro.ft_hangouts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.ugreyiro.ft_hangouts.adapter.ContactListAdapter
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.Gender

class MainActivity : AppCompatActivity() {

    private val contacts = listOf(
        Contact(id = 0, phoneNumber = "+33124124124", firstName = "John", lastName = "Holland"),
        Contact(id = 1, phoneNumber = "+781241415", firstName = "Alan", gender = Gender.MALE, comment = "Good guy"),
        Contact(id = 2, phoneNumber = "815812512512", firstName = "Tanya", lastName = "Lastname", gender = Gender.FEMALE)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val contactsView = findViewById<ListView>(R.id.contactsList)
        contactsView.adapter = ContactListAdapter(this, contacts)
    }
}