package com.ugreyiro.ft_hangouts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.ugreyiro.ft_hangouts.R
import com.ugreyiro.ft_hangouts.model.Message
import com.ugreyiro.ft_hangouts.model.MessageType
import java.text.DateFormat
import java.text.DateFormat.getDateTimeInstance
import java.util.concurrent.TimeUnit

class MessageListAdapter (
    context : Context,
    contacts : List<Message>
) : ArrayAdapter<Message>(context, R.layout.item_sent_message, contacts) {
    private val mInflater : LayoutInflater
    init {
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val message = getItem(position)!!
        val cv = if (message.type == MessageType.SENT) {
            mInflater.inflate(R.layout.item_sent_message, parent, false)
        } else {
            mInflater.inflate(R.layout.item_received_message, parent, false)
        }
        cv.findViewById<TextView>(R.id.msg_timestamp).text =
            getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(TimeUnit.MILLISECONDS.toMillis(message.date))
        cv.findViewById<TextView>(R.id.msg_text).text = message.body
        return cv
    }
}