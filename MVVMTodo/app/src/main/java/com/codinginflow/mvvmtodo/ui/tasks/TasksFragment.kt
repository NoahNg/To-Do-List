package com.codinginflow.mvvmtodo.ui.tasks
//this package is to organize all the fragments
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.FragmentTasksBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.codinginflow.mvvmtodo.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {

    private val viewModel: TasksViewModel by viewModels()

    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {//is called when this layout is instantiated
        super.onViewCreated(view, savedInstanceState)

        //we're not calling inflate because our layout is already inflated bc we pasted it onto the constructor Fragment(R.layout.fragment_tasks)
        val binding = FragmentTasksBinding.bind(view)

        val taskAdapter = TasksAdapter(this)

        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter //equivalent of calling binding.recyclerview.adapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(//ItemTouchHelper define what we want to happen when swipe an item or drag it
                0,//direction1
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT//direction2: be able to swipe into both direction
            ) {
                override fun onMove(//we dont care about onMove for this app bc onMove is up and down
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false//so we return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]//get the current item
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)//just have to add this after the delete to put the 2 together

            //add new task
            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        //the other half of the fragment result
        setFragmentResultListener("add_edit_request") { _, bundle ->//have to paste the exact same requestKey
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)//viewModel handles this event
        }

        //for fragments, we always want to past a life cycle. A fragment has 2 life cycles: the fragment object itself and when the fragment is put into a backstack
        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)//"it" is the new dataset
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {//launchWhenStarted makes the scope of this coroutine even smaller, bc instead getting cancelled when destroyView is called,
        //the coroutine will be cancelled when onStop is called, and resume when onStart is called
        //-> as soon as you put our fragments into the background, we don't listen for any events, bc we don't want to show our snack bar while our fragment is not visible,
            // instead the channel will wait, it will suspend, and when we bring the app back into the foreground, the coroutine will start again and it will start collecting
            //the events that are in the channel through this flow
            viewModel.tasksEvent.collect { event ->//gets pasted this stream of value
                when (event) {//bc later we will add more events and we want to distinguish them
                    is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {//when the viewmodel says that it's time to show the undo delete snack bar, the fragment will react to it
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)//the fragment now display the snackbar - like a toast
                            .setAction("UNDO") {//add our undo action
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                    is TasksViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(//from the navigation path
                                null,
                                "New Task"//title for the toolbar
                            )
                        findNavController().navigate(action)//activate this action
                    }
                    is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                event.task,
                                "Edit Task"//title for the toolbar
                            )
                        findNavController().navigate(action)
                    }
                    //show msg when use clicks the save button
                    is TasksViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    //delete all completed tasks
                    is TasksViewModel.TasksEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action =//create a new action
                            TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)//global action to our deleted all completed tasks screen/dialog
                    }
                }.exhaustive//turn this statement into an expression (From Utils.kt)
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    //options menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        //search
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value//read the current query from our viewModel
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()//expands the magnifying glass into the whole widget
            searchView.setQuery(pendingQuery, false)//put the query from the viewModel back to the search widget
        }

        searchView.onQueryTextChanged {//it's whatever we put into the listener function later
            viewModel.searchQuery.value = it
        }

        //read from a flow
        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted//first will only read one single value in the flow and then cancel it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            //hide completed tasks
            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                //viewModel.hideCompleted.value = item.isChecked //set its value to the current state of the checkbox
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }
            //delete all completed tasks
            R.id.action_delete_all_completed_tasks -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //will be called when the view of the fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
}