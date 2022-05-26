package com.codinginflow.mvvmtodo.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.databinding.FragmentAddEditTaskBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint//so our viewmodel is injected properly
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)//create binding variable

        //all the fields in our Edit Task
        binding.apply {
            editTextTaskName.setText(viewModel.taskName)//task name
            checkBoxImportant.isChecked = viewModel.taskImportance//the checkbox for if it's important or not
            checkBoxImportant.jumpDrawablesToCurrentState()//make the checkbox animation plays immediately
            textViewDateCreated.isVisible = viewModel.task != null//if we don't do this, it will be completely invisible
            textViewDateCreated.text = "Created: ${viewModel.task?.createdDateFormatted}"//date created

            //do something whenever we type in the edit text box
            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()//we want to take our ViewModel and update our task name with this new value -> send it to the AddEditTaskViewModel
            }

            //do same thing for checkBoxImportant
            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked//set it to this new value -> checked
            }

            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()//delegate the logic to the viewModel
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {//when check for a different event object

                    //remaining branches
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()//show our snackbar
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {//when we click the save button to navigate back
                        binding.editTextTaskName.clearFocus()//this hides the keyboard when we navigate back
                        //show the corresponding fragment for the snackbar
                        setFragmentResult(
                            "add_edit_request",//unique to identify different requests
                            bundleOf("add_edit_result" to event.result)//present data with a bundle with a key "add_edit_result" and "to" is an operator to map a key to its value
                        )
                        findNavController().popBackStack()//immediately remove this fragment from the backstack and go back to the previous one
                    }
                }.exhaustive//extension property to get compile time safety
            }
        }
    }
}