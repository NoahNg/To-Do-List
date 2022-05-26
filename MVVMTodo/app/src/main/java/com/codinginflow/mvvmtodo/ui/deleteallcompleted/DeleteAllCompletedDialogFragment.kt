package com.codinginflow.mvvmtodo.ui.deleteallcompleted

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint//bc it has its own viewModel
class DeleteAllCompletedDialogFragment : DialogFragment() {

    private val viewModel: DeleteAllCompletedViewModel by viewModels()//create a property for the viewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        //create a dialog with default layout
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm deletion")
            .setMessage("Do you really want to delete all completed tasks?")
            .setNegativeButton("Cancel", null)//"null" means this dialog will just be dismissed
            .setPositiveButton("Yes") { _, _ ->
                viewModel.onConfirmClick()//call viewModel to delete the operation
            }
            .create()//call ViewModel so we can actually delete the operation
}