package com.devanasmohammed.locationreminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.devanasmohammed.locationreminder.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    private val shopReminder =
        ReminderDTO(
            "Shop", "buy food for launch", "shop location",
            1.0, 1.0
        )
    private val pharmacyReminder =
        ReminderDTO(
            "pharmacy", "buy medicine for kids", "location2",
            2.0, 2.0
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
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetAll() = runTest {
        // GIVEN - Insert a reminder.
        database.reminderDao().saveReminder(shopReminder)
        database.reminderDao().saveReminder(pharmacyReminder)

        // WHEN - Get the task by all reminders from the database.
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data has the correct number of reminders
        assertThat(loaded.size, `is`(2))
    }

    @Test
    fun insetReminderAndGetById() = runTest {
        // GIVEN - Insert a task.
        database.reminderDao().saveReminder(shopReminder)

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminderById(shopReminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.title, `is`(shopReminder.title))
        assertThat(loaded.description, `is`(shopReminder.description))
        assertThat(loaded.location, `is`(shopReminder.location))
        assertThat(loaded.latitude, `is`(shopReminder.latitude))
        assertThat(loaded.longitude, `is`(shopReminder.longitude))
        assertThat(loaded.id, `is`(shopReminder.id))
    }


    @Test
    fun deleteAllReminder() = runTest {
        // GIVEN - Insert tasks .
        database.reminderDao().saveReminder(shopReminder)
        database.reminderDao().saveReminder(pharmacyReminder)
        // GIVEN - Delete all tasks .
        database.reminderDao().deleteAllReminders()

        // WHEN - get all reminders from database .
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data has the correct number of reminders
        assertThat(loaded.size, `is`(0))
    }
}