package com.devanasmohammed.locationreminder.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devanasmohammed.locationreminder.MainCoroutineRule
import com.devanasmohammed.locationreminder.R
import com.devanasmohammed.locationreminder.base.NavigationCommand
import com.devanasmohammed.locationreminder.data.FakeDataSource
import com.devanasmohammed.locationreminder.getOrAwaitValue
import com.devanasmohammed.locationreminder.locationreminders.data.ReminderDataSource
import com.devanasmohammed.locationreminder.locationreminders.data.dto.ReminderDTO
import com.devanasmohammed.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.devanasmohammed.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var reminderDataSource: ReminderDataSource

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

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        reminderDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @After
    fun cleanUp()  {
        stopKoin()
    }

    /**
     * Validate entered title and show snack bar title error
     */
    @Test
    fun validateEnteredData_invalidTitleShowSnackBar() {
        //enter wrong title
        val reminder = ReminderDataItem(null, "buy food for launch", "shop location", 1.0, 1.0)

        //validate entered data
        viewModel.validateEnteredData(reminder)
        val value = viewModel.showSnackBarInt.getOrAwaitValue()

        //assert that show enter title error
        Assert.assertEquals(value, R.string.err_enter_title)
    }

    /**
     * Validate entered title and show snack bar title error
     */
    @Test
    fun validateEnteredData_invalidLocationShowSnackBar() {
        //enter wrong location
        val reminder = ReminderDataItem("shop", "buy food for launch", null, 1.0, 1.0)

        //validate entered dat
        viewModel.validateEnteredData(reminder)
        val value = viewModel.showSnackBarInt.getOrAwaitValue()

        //assert that show enter location error
        Assert.assertEquals(value, R.string.err_select_location)
    }


    /**
     * Save reminder and show saved snack bar
     */
    @Test
    fun saveReminder_showSnackBarSaved() {
        //create a reminder
        val reminder = ReminderDataItem("shop", "buy food for launch", "shop location", 1.0, 1.0)

        //save reminder
        viewModel.saveReminder(reminder)

        //show toast
        val value = viewModel.showToast.getOrAwaitValue()

        //assert that value equals Reminder Saved !
        Assert.assertEquals(value, "Reminder Saved !")
    }

    /**
     * Save reminder and navigate back remindersList
     */
    @Test
    fun saveReminder_navigateBack() {
        //create a reminder
        val reminder = ReminderDataItem("shop", "buy food for launch", "shop location", 1.0, 1.0)

        //save reminder
        viewModel.saveReminder(reminder)

        //navigate back to ReminderListFragment
        val value = viewModel.navigationCommand.getOrAwaitValue()

        //assert that is navigates back
        Assert.assertEquals(value, NavigationCommand.Back)
    }

    /**
     * Save reminder and show loading
     */
    @Test
    fun saveReminder_showLoading(){
        //create a reminder
        val reminder = ReminderDataItem("shop", "buy food for launch", "shop location", 1.0, 1.0)

        //pause dispatcher to verify initial values.
        mainCoroutineRule.pauseDispatcher()

        //save reminder
        viewModel.saveReminder(reminder)

        //asset that show loading equals true
        val loadingValue = viewModel.showLoading.getOrAwaitValue()
        Assert.assertEquals(loadingValue, true)

        //execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        //asset that show loading equals false
        Assert.assertEquals(loadingValue, true)
    }


}