package com.codinginflow.mvvmtodo.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,//inject Dao
    @Assisted private val state: SavedStateHandle//dagger, in SavedStateHandle, we can store little piece of info when we create out UI but it also stores the navigation argument that we send over to this particular screen
    //bc we inject this ViewModel into our AddEditTask Fragment, this STH will automatically contain the arguments that we send to this fragment
) : ViewModel() {

    val task = state.get<Task>("task")//get our task from this state

    //split up this "task" object
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""//we want to be able to change the name of the task in our edit screen but we can't change it directly - unmutable -> retrieve this value from SavedInstanceState
        //? means that if this value is null, use the value on the right side
        //store this value in savedInstanceState
        set(value) {
            field = value//value is the input we do in the valuable
            state.set("taskName", value)//store this value in SIS
        }

    //do the same for taskImportance - Boolean value
    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()//Kotlin channel of type AddEditTaskEvent
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()//turn the addEditTaskEvent into a flow

    //save when the user click the save button when adding a new task
    fun onSaveClick() {
        //error control: if the task name is empty or only contain white spaces, display an error msg
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")//show invalid input msg
            return
        }

        //if task name is not null, we want to create a completely new task and add it to our database
        if (task != null) {
            //bc our task is immutable, we have to create a new object and send it to our database
            val updatedTask = task.copy(name = taskName, important = taskImportance)//copy the name and the importance
            updateTask(updatedTask)//method below
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)//method below
        }
    }

    //create Task
    private fun createTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))//navigate back
    }

    //update Task
    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))//navigate back
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {//launch a coroutine so we can send an event to our channel
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))//forward the text argument
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()//display the invalid input msg
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()//send the result back to the previous screen so the previous screen will decide which snack bar it will show
    }
}