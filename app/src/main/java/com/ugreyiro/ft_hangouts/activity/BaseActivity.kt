package com.ugreyiro.ft_hangouts.activity

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ugreyiro.ft_hangouts.db.FtHangoutsDatabaseHelper
import com.ugreyiro.ft_hangouts.db.HEADER_COLOR_SETTING_NAME
import com.ugreyiro.ft_hangouts.db.repository.ContactsRepository
import com.ugreyiro.ft_hangouts.db.repository.SettingsRepository
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.Message
import com.ugreyiro.ft_hangouts.observer.SmsPublisher

abstract class BaseActivity : AppCompatActivity(), ConfigurableHeaderActivity {

    abstract val settingsRepository : SettingsRepository
    open val contactsRepository = ContactsRepository(FtHangoutsDatabaseHelper(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        setHeaderColor()
        SmsPublisher.subscribe(SmsPublisher.ALL_MESSAGES_ADDRESS, ::onSmsReceived)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        SmsPublisher.unSubscribe(SmsPublisher.ALL_MESSAGES_ADDRESS)
        super.onDestroy()
    }

    override fun setHeaderColor() {
        val color = settingsRepository.settingValueByName(HEADER_COLOR_SETTING_NAME)
        setHeaderColor(color)
    }

    protected fun setHeaderColor(color : Int) {
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
    }

    fun hasPermission(permission: String, activity : Activity) =
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * Check received sms. Create new contacts if found messages from unrecorded contacts
     * @param messages list of received messages
     */
    open fun onSmsReceived(messages : List<Message>) {
        val recordedAddresses = contactsRepository.findAllAsListDto().map{ it.phoneNumber }.toSet()
        messages
            .map(Message::address)
            .toSet()
            .subtract(recordedAddresses) //choose only unrecorded
            .map { Contact(phoneNumber = it, firstName = it) } //create contacts
            .forEach { contactsRepository.create(it) } //save contact in DB
    }
}