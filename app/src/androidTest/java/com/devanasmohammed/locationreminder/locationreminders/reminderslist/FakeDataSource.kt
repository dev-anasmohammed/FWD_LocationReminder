package com.devanasmohammed.locationreminder.locationreminders.reminderslist

import com.devanasmohammed.locationreminder.locationreminders.data.ReminderDataSource
import com.devanasmohammed.locationreminder.locationreminders.data.dto.ReminderDTO
import com.devanasmohammed.locationreminder.locationreminders.data.dto.Result
import com.devanasmohammed.locationreminder.locationreminders.data.dto.Result.Error
import com.devanasmohammed.locationreminder.locationreminders.data.dto.Result.Success

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf())
    : ReminderDataSource {
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Error("Error while getting reminders")
        }
        return Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Error("Error while getting reminder with id: $id")
        }
        return try {
            Success(reminders.first { it.id == id })
        } catch (ex: Exception) {
            Error("Reminder not found")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}