package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource : FakeDataSource

    @Before
    fun initRemindersListViewModel(){
        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource)
    }

    @Test
    fun loadReminders_loadsRemindersIfAvailable() = runBlocking{
        //GIVEN reminder
        val reminder = ReminderDTO("title", "desc", "location",0.0, 0.0)

        //WHEN reminder saved to data source
        dataSource.saveReminder(reminder)

        //THEN data should be retrieved when loading
        val loaded = remindersListViewModel.loadReminders()
        assertThat(loaded, not(nullValue()))
    }

    @Test
    fun reminderslist_dataLoads() = runBlocking{
        //GIVEN data in the data source
        val reminder = ReminderDTO("title", "desc", "location",0.0, 0.0)
        dataSource.saveReminder(reminder)

        //WHEN loading data
        remindersListViewModel.loadReminders()

        //Then reminder list live data should have data
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), not(nullValue()))
    }

}