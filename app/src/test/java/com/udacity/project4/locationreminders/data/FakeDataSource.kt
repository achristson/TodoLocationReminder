package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var reminders : MutableList<ReminderDTO>? = mutableListOf()
    var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) return Result.Error("Test Error")
        reminders?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders?.find {it.id == id}
        if (reminder != null){
            return Result.Success(reminder)
        } else {
            return Result.Error("Reminder Not Found")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}