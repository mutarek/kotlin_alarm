package com.dhakadigital.kotlin_code_test

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhakadigital.kotlin_code_test.service.AlarmService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var addAlarmButton: Button
    private lateinit var selectedTimeText: TextView
    private lateinit var alarmTitleEdit: EditText
    private lateinit var alarmMessageEdit: EditText
    private lateinit var alarmsRecyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter

    private val calendar = Calendar.getInstance()
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        addAlarmButton = findViewById(R.id.add_alarm_button)
        selectedTimeText = findViewById(R.id.selected_time_text)
        alarmTitleEdit = findViewById(R.id.alarm_title_edit)
        alarmMessageEdit = findViewById(R.id.alarm_message_edit)
        alarmsRecyclerView = findViewById(R.id.alarms_recycler_view)

        // Setup RecyclerView
        alarmsRecyclerView.layoutManager = LinearLayoutManager(this)
        alarmAdapter = AlarmAdapter(
            mutableListOf(),
            onDeleteListener = { alarm ->
                AlarmService.cancelAlarm(this, alarm.id)
                loadAlarms()
            }
        )
        alarmsRecyclerView.adapter = alarmAdapter

        // Set current time as default
        updateSelectedTimeText()

        // Setup time picker button
        findViewById<Button>(R.id.pick_time_button).setOnClickListener {
            showDateTimePicker()
        }

        // Setup add alarm button
        addAlarmButton.setOnClickListener {
            val title = alarmTitleEdit.text.toString().takeIf { it.isNotEmpty() } ?: "Reminder"
            val message = alarmMessageEdit.text.toString()
            val timeInMillis = calendar.timeInMillis

            if (timeInMillis <= System.currentTimeMillis()) {
                Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Set the alarm
            val alarmId = AlarmService.setExactAlarm(
                this,
                timeInMillis,
                title,
                message
            )

            // Show confirmation
            Toast.makeText(
                this,
                "Alarm set for ${SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault()).format(Date(timeInMillis))}",
                Toast.LENGTH_LONG
            ).show()

            // Reset inputs
            alarmTitleEdit.setText("")
            alarmMessageEdit.setText("")

            // Reset calendar to current time
            calendar.timeInMillis = System.currentTimeMillis()
            updateSelectedTimeText()

            // Refresh alarm list
            loadAlarms()
        }

        // Load existing alarms
        loadAlarms()
    }

    private fun showDateTimePicker() {
        // Date picker
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Time picker
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        updateSelectedTimeText()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateSelectedTimeText() {
        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy - h:mm a", Locale.getDefault())
        selectedTimeText.text = "Selected Time: ${dateFormat.format(calendar.time)}"
    }

    private fun loadAlarms() {
        val alarms = AlarmService.getSavedAlarms(this)
        alarmAdapter.updateAlarms(alarms)

        // Show/hide empty view
        findViewById<View>(R.id.empty_view).visibility =
            if (alarms.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadAlarms()
    }
}