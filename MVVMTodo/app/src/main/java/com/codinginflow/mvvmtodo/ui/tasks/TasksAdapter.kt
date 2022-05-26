package com.codinginflow.mvvmtodo.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.ItemTaskBinding


class TasksAdapter(private val listener: OnItemClickListener) ://handle a list of tasks and display approriate animation whenever the list changes
    ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {//use ListAdapter when you have a completely new list pasted to you

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)//instantiate our item task layout and turn it into an object that our binding class can use
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TasksViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        //update the completed state
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {//error check in case the position is -1 and the app will crash
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
                checkBoxCompleted.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {//error check in case the position is -1 and the app will crash
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, checkBoxCompleted.isChecked)
                    }
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                checkBoxCompleted.isChecked = task.completed
                textViewName.text = task.name
                textViewName.paint.isStrikeThruText = task.completed
                labelPriority.isVisible = task.important
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task: Task, isChecked: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =//means that 2 items represent the same logical item. For example, the item can change its position in the list without changing its actual content
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task) =//tell the callback that the content has changed -> the adapter knows that it has to update this particular item
            oldItem == newItem
    }
}