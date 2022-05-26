package com.codinginflow.mvvmtodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

//Room is better because it throws compile time errors
@Dao //Dao stands for Data Access Object, an interface where we declare different methods, and different database operations (insert, delete, update, etc).
interface TaskDao {

    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Task>> =
        when (sortOrder) {
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompleted)
        }

    //make the query that returns our tasks in task table
    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name") //filter our tasks by name and searchQuery string is somewhere in this name
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>> //flow represents a stream of data

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, created")
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    //task_table is represented by task_data class
    //able to insert new tasks into database
    @Insert(onConflict = OnConflictStrategy.REPLACE)//annotation @Insert later generates the necessary code for the Insert operation
    //REPLACE means that if there is another task with the same ID, it will just replace it
    suspend fun insert(task: Task)//suspend switches the current task into a different thread. Using this bc insert can take time and it will block the main code and will be visible as a line on our UI.
    //However, room wont let us execute any task on the main thread -> will throw error
    //suspend function can be paused and resumed later on

    //do the same for update and delete
    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    //for deleting existing tasks
    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteCompletedTasks()
}