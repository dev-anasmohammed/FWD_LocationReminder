package com.devanasmohammed.locationreminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.devanasmohammed.locationreminder.locationreminders.data.dto.ReminderDTO
import com.devanasmohammed.locationreminder.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    //testing implementation to the RemindersLocalRepository.kt
    private lateinit var localDataSource: RemindersLocalRepository

    private lateinit var database: RemindersDatabase

    private val shopReminder =
        ReminderDTO(
            "Shop", "buy food for launch", "shop location",
            1.0, 1.0
        )

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        localDataSource = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun saveReminder_retrieveReminderByID() = runTest {
        // GIVEN - A new reminder saved in the database.
        database.reminderDao().saveReminder(shopReminder)

        // WHEN  - Task retrieved by ID.
        val result = localDataSource.getReminder(shopReminder.id)

        // THEN - Same task is returned.
        result as Result.Success
        MatcherAssert.assertThat(result.data.title, Is.`is`("Shop"))
        MatcherAssert.assertThat(result.data.description, Is.`is`("buy food for launch"))
        MatcherAssert.assertThat(result.data.location, Is.`is`(shopReminder.location))
        MatcherAssert.assertThat(result.data.longitude, Is.`is`(shopReminder.longitude))
        MatcherAssert.assertThat(result.data.latitude, Is.`is`(shopReminder.latitude))
    }

    @Test
    fun getRemindersWithNoReminders_returnEmptyList() = runTest {
        // GIVEN - No reminders saved in the database.

        // WHEN  - get reminders.
        val result = localDataSource.getReminders()

        // THEN -return empty list
        result as Result.Success
        MatcherAssert.assertThat(result.data, Is.`is`(emptyList()))
    }

    @Test
    fun getReminderNotExist_returnReminderNotFound() = runTest {
        // GIVEN - wrong id
        val reminderId = "990"

        // WHEN  - reminder retrieved by ID.
        val result = localDataSource.getReminder(reminderId)

        // THEN - return Reminder not found!
        result as Result.Error
        MatcherAssert.assertThat(result.message, `is`("Reminder not found!"))
    }

}