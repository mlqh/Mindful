package com.ece452s24g7.mindful.activities.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ece452s24g7.mindful.database.Entry
import com.ece452s24g7.mindful.database.EntryDatabase
import kotlinx.coroutines.launch
import java.util.Date

class CreateEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = EntryDatabase.getInstance(application).dao()

    fun saveEntry(
        id: Int,
        entryDate: Date,
        entryBody: String,
        entryImageURIs: List<String>,
        entryVideoURIs: List<String>,
        entryAudioPath: String?,
        currentLocation: String?
    ) {
        viewModelScope.launch {
            if (id == -1) {
                dao.insert(Entry(
                    date = entryDate,
                    text = entryBody,
                    imageURIs = entryImageURIs,
                    videoURIs = entryVideoURIs,
                    audioPath = entryAudioPath,
                    location = currentLocation
                ))
            } else {
                dao.update(Entry(
                    uid = id,
                    date = entryDate,
                    text = entryBody,
                    imageURIs = entryImageURIs,
                    videoURIs = entryVideoURIs,
                    audioPath = entryAudioPath,
                    location = currentLocation
                ))
            }
        }
    }
}