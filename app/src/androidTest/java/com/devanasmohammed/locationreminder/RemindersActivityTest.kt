package com.devanasmohammed.locationreminder

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.devanasmohammed.locationreminder.locationreminders.RemindersActivity
import com.devanasmohammed.locationreminder.locationreminders.data.ReminderDataSource
import com.devanasmohammed.locationreminder.locationreminders.data.local.LocalDB
import com.devanasmohammed.locationreminder.locationreminders.data.local.RemindersLocalRepository
import com.devanasmohammed.locationreminder.locationreminders.reminderslist.RemindersListViewModel
import com.devanasmohammed.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.devanasmohammed.locationreminder.util.DataBindingIdlingResource
import com.devanasmohammed.locationreminder.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    // get activity context
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Testing add reminder by selecting location
     * and enter title and description
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun addReminder() = runTest {

        //start up tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //click on add reminder button
        onView(withId(R.id.addReminderFAB)).perform(click())

        //go to select location
        onView(withId(R.id.selectLocation)).perform(click())

        //add marker on map, and save location
        onView(withContentDescription("Google Map")).perform(longClick())
        onView(withId(R.id.save_btn)).perform(click())

        //add title and description
        onView(withId(R.id.reminderTitle)).perform(replaceText("Shop"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("buy products from the shop"))

        //click on save reminder
        onView(withId(R.id.saveReminder)).perform(click())

        //testing saving toast
        onView(withText(R.string.reminder_saved))
            .inRoot(withDecorView(not(`is`(getActivity(activityScenario)?.window?.decorView))))
            .check(matches(isDisplayed()))

        //verify reminder is displayed on screen in the reminder list.
        onView(withText("Shop")).check(matches(isDisplayed()))

        //close activity resetting the db.
        activityScenario.close()
    }

    /**
     * Test error enter title
     */
    @Test
    fun showSnackBar_errorEnterTitle(){
        //start up tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //click on fab to add reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        //click on save reminder
        onView(withId(R.id.saveReminder)).perform(click())

        //check that snack bar shows error
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        //close activity resetting the db.
        activityScenario.close()

    }

    /**
     * Test error select location
     */
    @Test
    fun showSnackBar_errorEnterLocation(){
        //start up tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //click on fab to add reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        //click on edittext to enter title of reminder
        onView(withId(R.id.reminderTitle)).perform(typeText("Shop"), closeSoftKeyboard())

        //click on edittext to enter title of description
        onView(withId(R.id.reminderDescription)).perform(typeText("buy prodcuts from the shop"), closeSoftKeyboard())

        //click on save reminder
        onView(withId(R.id.saveReminder)).perform(click())

        //check that snack bar shows error
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))

        //close activity resetting the db.
        activityScenario.close()
    }



}
