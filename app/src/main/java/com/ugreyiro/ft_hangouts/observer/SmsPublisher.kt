package com.ugreyiro.ft_hangouts.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.ugreyiro.ft_hangouts.model.Message
import com.ugreyiro.ft_hangouts.model.MessageType

/** Publisher of received sms events */
object SmsPublisher: BroadcastReceiver() {

    const val ALL_MESSAGES_ADDRESS = "ALL"


    private val subscribers : MutableMap<String, (List<Message>) -> Unit> = mutableMapOf()

    /** Notify subscribers about received new messages */
    override fun onReceive(context: Context?, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            .map {
                Message(
                    System.currentTimeMillis(),
                    it.displayMessageBody,
                    MessageType.RECEIVED,
                    it.displayOriginatingAddress
                )
            }
        val phoneMessageMap = mutableMapOf<String, MutableList<Message>>()
        messages.forEach { message ->
            phoneMessageMap.computeIfAbsent(message.address) { mutableListOf() } += message
        }
        //notify subscribers
        phoneMessageMap.forEach {
                (address, msgs) -> subscribers[address]?.invoke(msgs.toList())
        }
        subscribers[ALL_MESSAGES_ADDRESS]?.invoke(messages.toList())
    }

    /**
     * Provide subscription on received sms on chosen address
     * @param address - subscribe on sms from this phone number
     * @param action - action will be called when sms will be received
     */
    fun subscribe(address : String, action : (List<Message>) -> Unit) {
        subscribers[address] = action
    }

    fun unSubscribe(address : String) {
        subscribers.remove(address)
    }
}