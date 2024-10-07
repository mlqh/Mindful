package com.ece452s24g7.mindful.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ece452s24g7.mindful.R
import com.ece452s24g7.mindful.activities.decorators.EntryDecorator
import com.ece452s24g7.mindful.activities.viewmodels.CalendarViewModel
import com.ece452s24g7.mindful.adapters.EntryListAdapter
import com.ece452s24g7.mindful.database.Entry
import com.ece452s24g7.mindful.databinding.ActivityCalendarBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate
import java.time.ZoneId

class CalendarActivity : AppCompatActivity() {
    private val viewModel by viewModels<CalendarViewModel>()
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var entryListAdapter: EntryListAdapter
    private val dateEntryMap = mutableMapOf<CalendarDay, MutableList<Entry>>()
    private var selectedDate: CalendarDay = CalendarDay.today()
    private var calendarVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeMonthEntries()
        setupCalendarView()

        val calendarCreateButton = binding.calendarCreateButton
        calendarCreateButton.setOnClickListener {
            val createEntryIntent = Intent(this, CreateEntryActivity::class.java)
            startActivity(createEntryIntent)
        }

        val calendarExpandButton = binding.calendarExpandButton
        val calendarView = binding.calendarView
        calendarExpandButton.setOnClickListener {
            if (calendarVisible) {
                calendarView.visibility = View.GONE
                calendarExpandButton.setImageResource(R.drawable.baseline_expand_more_36)
            } else {
                calendarView.visibility = View.VISIBLE
                calendarExpandButton.setImageResource(R.drawable.baseline_expand_less_36)
            }
            calendarVisible = !calendarVisible
        }
    }

    override fun onResume() {
        super.onResume()
        observeMonthEntries()
        setupCalendarView()
    }

    // fetches entries and stores in map, updates decorators
    private fun observeMonthEntries() {
        viewModel.getMonthEntries(selectedDate).observe(this) { entries ->
            dateEntryMap.clear()
            // group entries by date
            for (entry in entries) {
                val date: LocalDate = entry.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val calDay = CalendarDay.from(date.year, date.monthValue, date.dayOfMonth)
                dateEntryMap.getOrPut(calDay) { mutableListOf() }.add(entry)
            }
            updateCalendarDecorators()
            refreshEntriesForSelectedDate() // Refresh entries for the currently selected date
        }
    }

    private fun updateCalendarDecorators() {
        val calendarView = binding.calendarView
        calendarView.addDecorator(EntryDecorator(this, dateEntryMap, 1, false))
        calendarView.addDecorator(EntryDecorator(this, dateEntryMap, 2, false))
        calendarView.addDecorator(EntryDecorator(this, dateEntryMap, 3, true))
    }

    private fun setupCalendarView() {
        val calendarView = binding.calendarView
        calendarView.setOnDateChangedListener { _, date, selected ->
            if (selected) {
                selectedDate = date
                val maybeEntries: MutableList<Entry>? = dateEntryMap[date]
                if (!maybeEntries.isNullOrEmpty()) {
                    val entries: List<Entry> = maybeEntries.toList()
                    populateEntriesView(entries)
                } else {
                    populateEntriesView(mutableListOf())
                }
            }
        }
        calendarView.setOnMonthChangedListener { _, date ->
            selectedDate = date
            observeMonthEntries()
        }
    }

    private fun populateEntriesView(entries: List<Entry>) {
        val entryListView = binding.entryList
        entryListView.layoutManager = LinearLayoutManager(this)
        entryListAdapter = EntryListAdapter(entries, this) {
            refreshEntriesForSelectedDate()
        }
        entryListView.adapter = entryListAdapter
    }

    private fun refreshEntriesForSelectedDate() {
        selectedDate.let { date ->
            val maybeEntries: MutableList<Entry>? = dateEntryMap[date]
            if (!maybeEntries.isNullOrEmpty()) {
                val entries: List<Entry> = maybeEntries.toList()
                populateEntriesView(entries)
            } else {
                populateEntriesView(mutableListOf())
            }
        }
    }
}