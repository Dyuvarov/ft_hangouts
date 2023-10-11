package com.ugreyiro.ft_hangouts.activity

import android.content.ContentResolver
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ugreyiro.ft_hangouts.R
import com.ugreyiro.ft_hangouts.adapter.MessageListAdapter
import com.ugreyiro.ft_hangouts.model.Message
import com.ugreyiro.ft_hangouts.model.MessageType
import com.ugreyiro.ft_hangouts.observer.SmsPublisher

class ChatActivity : AppCompatActivity() {
    private lateinit var messagesListView : ListView
    private lateinit var inputMessageEditText : EditText
    private lateinit var contactNameTextView: TextView
    private lateinit var sendBtn : Button

    private val messages : MutableList<Message> = mutableListOf()
    private lateinit var smsManager : SmsManager
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
        smsManager = getSmsManager()
        initSendBtn()

        contactPhoneNumber = intent.getStringExtra(PHONE_NUMBER_EXTRA) ?:
                throw IllegalStateException("Contact phone number not specified")
        contactName = intent.getStringExtra(FIRST_NAME_EXTRA) ?:
                throw IllegalStateException("Contact name not specified")
        contactNameTextView.text = contactName

        fillMessagesList()

        SmsPublisher.subscribe(contactPhoneNumber, ::onNewMessages)
    }

    override fun onDestroy() {
        SmsPublisher.unSubscribe(contactPhoneNumber)
        super.onDestroy()
    }

    private fun fillMessagesList() {
        if (messages.isEmpty()) {
            initMessages()
        }
        messagesListView.adapter = MessageListAdapter(this, messages)
    }

    private fun initMessages() {
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            "${Telephony.Sms.ADDRESS} = ?",
            arrayOf(contactPhoneNumber),
            Telephony.Sms.DATE
        )
        cursor?.use {
            while(cursor.moveToNext()) {
                val dateRaw = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.DATE)).toLong()
                val text = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val typeRaw = it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.TYPE))
                val type = if (typeRaw == MESSAGE_TYPE_INBOX) MessageType.RECEIVED else MessageType.SENT
                messages.add(Message(dateRaw, text, type, contactPhoneNumber))
            }
        }
    }

    private fun initSendBtn() {
        sendBtn = findViewById(R.id.send_msg_btn)
        sendBtn.setOnClickListener { onSendBtnClick() }
    }

    private fun onSendBtnClick() {
        val messageText = inputMessageEditText.text.toString()
        if (messageText.isNotBlank()) {
            smsManager.sendTextMessage(contactPhoneNumber, null, messageText, null, null)
            inputMessageEditText.setText("")
            messages.add(Message(
                date = System.currentTimeMillis(),
                body = messageText,
                type = MessageType.SENT,
                address = "")
            )
            fillMessagesList()
        }
    }

    private fun getSmsManager() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        applicationContext.getSystemService(SmsManager::class.java)
    } else {
        SmsManager.getDefault()
    }

    private fun onNewMessages(receivedMessages : List<Message>) {
        messages += receivedMessages
        messagesListView.adapter = MessageListAdapter(this, messages)
    }
}