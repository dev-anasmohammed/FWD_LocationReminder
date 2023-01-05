package com.devanasmohammed.locationreminder.data

import com.devanasmohammed.locationreminder.locationreminders.data.ReminderDataSource
import com.devanasmohammed.locationreminder.locationreminders.data.dto.ReminderDTO
import com.devanasmohammed.locationreminder.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf())
    : ReminderDataSource {
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Error while getting reminders")
        }
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Error while getting reminder with id: $id")
        }
        return try {
            Result.Success(reminders.first { it.id == id })
        } catch (ex: Exception) {
            Result.Error("Reminder not found")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

}