package com.dhakadigital.kotlin_code_test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmAdapter(
    private val alarms: MutableList<AlarmData>,
    private val onDeleteListener: (AlarmData) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.alarm_item_title)
        val timeText: TextView = view.findViewById(R.id.alarm_item_time)
        val messageText: TextView = view.findViewById(R.id.alarm_item_message)
        val deleteButton: Button = view.findViewById(R.id.alarm_item_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.titleText.text = alarm.title

        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy - h:mm a", Locale.getDefault())
        holder.timeText.text = dateFormat.format(Date(alarm.timeInMillis))

        // Show or hide message
        if (alarm.message.isNotEmpty()) {
            holder.messageText.visibility = View.VISIBLE
            holder.messageText.text = alarm.message
        } else {
            holder.messageText.visibility = View.GONE
        }

        // Setup delete button
        holder.deleteButton.setOnClickListener {
            onDeleteListener(alarm)
        }
    }

    override fun getItemCount() = alarms.size

    fun updateAlarms(newAlarms: List<AlarmData>) {
        alarms.clear()
        alarms.addAll(newAlarms)
        notifyDataSetChanged()
    }
}