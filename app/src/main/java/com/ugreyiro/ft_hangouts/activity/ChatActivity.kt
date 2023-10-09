package com.ugreyiro.ft_hangouts.activity

import android.content.ContentResolver
import android.os.Bundle
import android.provider.Telephony
import android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ugreyiro.ft_hangouts.R
import com.ugreyiro.ft_hangouts.adapter.ContactListAdapter
import com.ugreyiro.ft_hangouts.adapter.MessageListAdapter
import com.ugreyiro.ft_hangouts.model.Message
import com.ugreyiro.ft_hangouts.model.MessageType
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    private val datePattern = "dd/MM/yy HH:MM"

    private lateinit var messagesListView : ListView
    private lateinit var inputMessageEditText : EditText
    private lateinit var contactNameTextView: TextView
    private lateinit var sendBtn : Button

    private lateinit var contentResolver : ContentResolver
    private lateinit var contactPhoneNumber : String
    private lateinit var contactName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        contentResolver = applicationContext.contentResolver

        messagesListView = findViewById(R.id.messages_list)
        inputMessageEditText = findViewById(R.id.input_msg_text)
        contactNameTextView = findViewById(R.id.chat_contact_name)
        sendBtn = findViewById(R.id.send_msg_btn)

        contactPhoneNumber = intent.getStringExtra(PHONE_NUMBER_EXTRA) ?:
            throw IllegalStateException("Contact phone number not specified")
        contactName = intent.getStringExtra(FIRST_NAME_EXTRA) ?:
                throw IllegalStateException("Contact name not specified")
        contactNameTextView.text = contactName

        fillMessagesList()
    }

    private fun fillMessagesList() {
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            "${Telephony.Sms.ADDRESS} = ?",
            arrayOf(contactPhoneNumber),
            Telephony.Sms.DATE
        )
        cursor?.use {
            val messages = mutableListOf<Message>()
            while(cursor.moveToNext()) {
                val dateRaw = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.DATE)).toLong()
                val text = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val typeRaw = it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.TYPE))
                val type = if (typeRaw == MESSAGE_TYPE_INBOX) MessageType.RECEIVED else MessageType.SENT
                val date = SimpleDateFormat(datePattern)
                    .format(TimeUnit.MILLISECONDS.toMillis(dateRaw))
                messages.add(Message(date, text, type))
            }
            messagesListView.adapter = MessageListAdapter(this, messages)
        }
    }
}