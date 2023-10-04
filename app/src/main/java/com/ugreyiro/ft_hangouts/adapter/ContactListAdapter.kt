package com.ugreyiro.ft_hangouts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.ugreyiro.ft_hangouts.R
import com.ugreyiro.ft_hangouts.model.Contact
import com.ugreyiro.ft_hangouts.model.fullName

class ContactListAdapter(
    context : Context,
    contacts : List<Contact>
) : ArrayAdapter<Contact>(context, R.layout.item_contact, contacts) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val cv = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_contact, null)
        val contact = getItem(position)
        if (contact != null) {
            cv.findViewById<TextView>(R.id.contactFullName).text = contact.fullName()
            cv.findViewById<TextView>(R.id.contactPhoneNumber).text = contact.phoneNumber
        }
        return cv
    }
}