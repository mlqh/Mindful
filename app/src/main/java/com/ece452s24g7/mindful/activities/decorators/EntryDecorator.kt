package com.ece452s24g7.mindful.activities.decorators

import android.app.Activity
import androidx.core.content.ContextCompat
import com.ece452s24g7.mindful.R
import com.ece452s24g7.mindful.database.Entry
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class EntryDecorator (
    private val context: Activity,
    private val dateEntryMap: Map<CalendarDay, List<Entry>>,
    private val entryCount: Int,
    private val isMax: Boolean
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dateEntryMap.containsKey(day) && if (isMax) dateEntryMap[day]!!.size >= entryCount else dateEntryMap[day]!!.size == entryCount
    }

    override fun decorate(view: DayViewFacade) {
        val colors = listOf(
            ContextCompat.getColor(context, R.color.colorAccent1),
            ContextCompat.getColor(context, R.color.colorAccent2),
            ContextCompat.getColor(context, R.color.colorAccent3)
        )
        view.addSpan(MultiDotSpan(8f, colors, entryCount))
    }
}