package com.ugreyiro.ft_hangouts.activity

import android.content.Intent
import android.icu.text.DateFormat.getDateTimeInstance
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.ugreyiro.ft_hangouts.R
import com.ugreyiro.ft_hangouts.adapter.ContactListAdapter
import com.ugreyiro.ft_hangouts.db.FtHangoutsDatabaseHelper
import com.ugreyiro.ft_hangouts.db.HEADER_COLOR_SETTING_NAME
import com.ugreyiro.ft_hangouts.db.repository.ContactsRepository
import com.ugreyiro.ft_hangouts.db.repository.SettingsRepository
import com.ugreyiro.ft_hangouts.exception.EntryNotFoundException
import com.ugreyiro.ft_hangouts.model.Message
import com.ugreyiro.ft_hangouts.observer.MainActivityBackgroundObserver
import java.util.Date

class MainActivity : BaseActivity() {
    private lateinit var contactsListView : ListView
    private lateinit var addContactBtn : ImageButton

    private val dbHelper = FtHangoutsDatabaseHelper(this)
    override val contactsRepository = ContactsRepository(dbHelper)
    override val settingsRepository = SettingsRepository(dbHelper)
    private val contactActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            fillContactsList()
        }

    /** Stores timestamp when app was set in background */
    private var setInBackgroundAt : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        initContactsListView()
        initAddContactButton()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_app_header, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.headerColumnMenu) {
            val newColor = when (item.itemId) {
                R.id.color2 -> getColor(R.color.dark_blue)
                R.id.color3 -> getColor(R.color.pink)
                R.id.color4 -> getColor(R.color.purple)
                else -> getColor(R.color.black)
            }
            settingsRepository.updateSetting(HEADER_COLOR_SETTING_NAME, newColor.toString())
            setHeaderColor(newColor)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSmsReceived(messages: List<Message>) {
        super.onSmsReceived(messages)
        fillContactsList()
    }

    override fun onStart() {
        super.onStart()
        if (MainActivityBackgroundObserver.returnedToFront() && setInBackgroundAt != null) {
            showSetBackgroundToast()
            setInBackgroundAt = null
        }
    }

    override fun onStop() {
        super.onStop()
        if (MainActivityBackgroundObserver.setInBackground() && !isChangingConfigurations) {
            setInBackgroundAt = getDateTimeInstance().format(Date())
        }
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
            contactsRepository.findAllAsListDto()
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
            contactsRepository.findContactById(contactId)
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
            it.putExtra("lastName", contact.lastName ?: "")
            it.putExtra("gender", contact.gender.name)
            it.putExtra("comment", contact.comment ?: "")
        }
        contactActivityLauncher.launch(intent)
    }

    private fun showSetBackgroundToast() {
        Toast.makeText(
            this,
            "${getString(R.string.set_background_date_time)} $setInBackgroundAt",
            Toast.LENGTH_LONG
        ).show()
    }
}