package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RemindersListViewModelTest {

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Use a fake repository to be injected into the viewmodel
    @Mock
    private lateinit var dataSource: ReminderDataSource

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var observer: Observer<List<ReminderDataItem>>

    @Mock
    private lateinit var showSnackBar: Observer<String>

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        Dispatchers.setMain(Dispatchers.Default)
        remindersListViewModel = RemindersListViewModel(application, dataSource)
        remindersListViewModel.remindersList.observeForever(observer)
        remindersListViewModel.showSnackBar.observeForever(showSnackBar)
    }

    @After
    fun tearDown() {
        remindersListViewModel.remindersList.removeObserver(observer)
        remindersListViewModel.showSnackBar.removeObserver(showSnackBar)
        Dispatchers.resetMain()
    }

    @Test
    fun `loadReminders success`() = runTest {
        //arrange
        val reminderDTOList = listOf(
            ReminderDTO("Title 1", "Description 1", "Location 1", 0.0, 0.0, "1"),
        )
        val expected = reminderDTOList.map { reminder ->
            ReminderDataItem(
                reminder.title,
                reminder.description,
                reminder.location,
                reminder.latitude,
                reminder.longitude,
                reminder.id
            )
        }
        Mockito.`when`(dataSource.getReminders()).thenReturn(Result.Success(reminderDTOList))

        //act
        remindersListViewModel.loadReminders()

        //verify
        Mockito.verify(observer).onChanged(expected)
        Assert.assertEquals(expected, remindersListViewModel.remindersList.value)

    }

    @Test
    fun `loadReminders error`() = runTest {
        //arrange
        val expected = "error"
        Mockito.`when`(dataSource.getReminders()).thenReturn(Result.Error(expected))

        //act
        remindersListViewModel.loadReminders()

        advanceUntilIdle()

        //verify
        Mockito.verify(showSnackBar).onChanged(expected)
        Assert.assertEquals(expected, remindersListViewModel.showSnackBar.value)
    }
}