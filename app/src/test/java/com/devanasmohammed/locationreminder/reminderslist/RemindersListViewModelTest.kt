package com.devanasmohammed.locationreminder.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devanasmohammed.locationreminder.MainCoroutineRule
import com.devanasmohammed.locationreminder.data.FakeDataSource
import com.devanasmohammed.locationreminder.getOrAwaitValue
import com.devanasmohammed.locationreminder.locationreminders.data.dto.ReminderDTO
import com.devanasmohammed.locationreminder.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import com.google.common.truth.Truth.assertThat

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var reminderDataSource: FakeDataSource

    private val shopReminder =
        ReminderDTO(
            "Shop", "buy food for launch", "shop location",
            1.0, 1.0
        )
    private val pharmacyReminder =
        ReminderDTO(
            "pharmacy", "buy medicine for kids", "pharmacy location",
            2.0, 2.0
        )

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        reminderDataSource = FakeDataSource()
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @After
    fun cleanUp() {
        stopKoin()
    }

    /**
     * Load reminder and show data
     */
    @Test
    fun loadReminders_showData() = runTest {
        //save reminders
        reminderDataSource.saveReminder(shopReminder)
        reminderDataSource.saveReminder(pharmacyReminder)

        //load reminders
        viewModel.loadReminders()

        val value = viewModel.showNoData.getOrAwaitValue()

        //assert that showNoData is false .
        Assert.assertEquals(value, false)
    }

    /**
     * Load reminder and show loading
     */
    @Test
    fun loadReminders_showLoading() {
        //pause dispatcher to verify initial values.
        mainCoroutineRule.pauseDispatcher()

        //load the task in the view model.
        viewModel.loadReminders()

        //assert that the progress indicator is shown.
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), Is.`is`(true))

        //execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        //then assert that the progress indicator is hidden.
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), Is.`is`(false))
    }

    /**
     * load reminders and show error
     */
    @Test
    fun loadReminders_showError() {
        //pause dispatcher to verify initial values.
        mainCoroutineRule.pauseDispatcher()

        //show error.
        reminderDataSource.setReturnError(true)

        //load reminders
        viewModel.loadReminders()

        //execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        //assert that error showing with title Error while getting reminders
        assertThat(viewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error while getting reminders")
    }



}