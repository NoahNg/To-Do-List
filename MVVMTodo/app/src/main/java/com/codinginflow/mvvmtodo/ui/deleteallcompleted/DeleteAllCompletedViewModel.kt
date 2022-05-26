package com.codinginflow.mvvmtodo.ui.deleteallcompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {
/*
* When we click the delete button on a dialog, the action will happen immediately and automatically, not after we call navigate
* But in an operation of deleting multiple items in a database can take a few milliseconds.
* So what happens when we launch this operation in viewModel scope is that the fragment dialog is dismissed and the viewModel gets removed from memory together with it bc the fragment is over
* and the viewModel scope is actually cancelled.
* So if we launch this scope, what happens is that our operation will get cancelled somewhere in the middle -> we need larger scope and that's why we use Application Scope bc this one is not cancelled
* when the ViewModel is removed from the memory */
    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }
}