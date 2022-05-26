package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    //    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
    //    val hideCompleted = MutableStateFlow(false)


    val preferencesFlow = preferencesManager.preferencesFlow

    /*private val tasksFlow = searchQuery.flatMapLatest { //flatMapLatest is a flow operator - whenever the value of searchQuery flow changes/when we type into the search field, it will execute the below block
        //and the parameter we get passed is the current value of the searchQuery. Use this value, run our SQLite query again and then assign the result to the task flow
        taskDao.getTasks(it)
    }*/

    private val tasksEventChannel = Channel<TasksEvent>()//means that we can put TasksEvent kind of data into the channel
    val tasksEvent = tasksEventChannel.receiveAsFlow()//turn this channel into a flow, then we can use fragment to get a single value out of it

    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val tasks = tasksFlow.asLiveData()

    /*the whole flow is that we type something as the search field in searchView.onQueryTextChanged in TasksFragment.kt
    * it wwill change the value of our searchQuery flow to the new query string
    * This will trigger the flatMapLatest operator, execute our search in Dao and the return value to the tasksFlow, which is observed as live data*/

    //functions with that our fragment can then use to update the values in the preferences manager
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    //this will be called when we click an item on our RecyclerView
    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    /*
    * the flow: we have 3 different mutablestateflow that we can change in our UI, and then we combine all of them into a single flow. Whenever a value changes, we will get the latest value of ALL OF THEM
    * then we can use the latest values for our query*/

    fun onTaskSwiped(task: Task) = viewModelScope.launch {//because we want to do delete, we have to launch a coroutine function
        taskDao.delete(task)//bc we have the @Delete, we dont have to do anything else bc the system knows what to do already
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))//display the task delete message
    }

    //sealed class TasksEvent {
    //  data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
    //}

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen) //launched a coroutine because we want to send an event to our channel and we can only do this with a coroutine
    }

    //when the user click the save button
    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")//when a new task is added
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")//when a task is edited
        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))//emit event from our viewModel so our fragment can listen to them and take appropriate action
    }

    //delete all completed tasks
    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)
    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()//object bc we only want to send one event of this type - 1 instance of this class
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()//navigate to edit task screen
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()//undo delete task message
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()//show msg when the user clicks the save button
        object NavigateToDeleteAllCompletedScreen : TasksEvent()//object bc it doesnt take any arguments
    }
}