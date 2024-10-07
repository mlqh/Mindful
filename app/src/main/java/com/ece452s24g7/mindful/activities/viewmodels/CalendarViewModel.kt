package com.ece452s24g7.mindful.activities.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ece452s24g7.mindful.database.Entry
import com.ece452s24g7.mindful.database.EntryDatabase
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate
import java.time.ZoneId

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = EntryDatabase.getInstance(application).dao()

    fun getMonthEntries(selectedDate: CalendarDay): LiveData<List<Entry>>{
        val today: LocalDate = LocalDate.of(selectedDate.year, selectedDate.month, selectedDate.day)
        val zoneId = ZoneId.systemDefault()
        val firstOfThisMonth: LocalDate = today.withDayOfMonth(1)
        val endOfThisMonth: LocalDate = today.withDayOfMonth(today.month.length(today.isLeapYear))

        val firstDayLong: Long = firstOfThisMonth.atStartOfDay(zoneId).toEpochSecond() * 1000
        val lastDayLong: Long = endOfThisMonth.atStartOfDay(zoneId).toEpochSecond() * 1000

        val entries: LiveData<List<Entry>> = dao.getEntriesInMonth(firstDayLong, lastDayLong).asLiveData()
        return entries
    }

}