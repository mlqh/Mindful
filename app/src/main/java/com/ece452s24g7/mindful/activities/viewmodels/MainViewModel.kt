package com.ece452s24g7.mindful.activities.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ece452s24g7.mindful.database.Entry
import com.ece452s24g7.mindful.database.EntryDatabase

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var dao = EntryDatabase.getInstance(application).dao()
    var entries: LiveData<List<Entry>> = dao.getAll().asLiveData()

    fun refreshDatabase() {
        dao = EntryDatabase.getInstance(getApplication()).dao()
        entries = dao.getAll().asLiveData()
    }
}