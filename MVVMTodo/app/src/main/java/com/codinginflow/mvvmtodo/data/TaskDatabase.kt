package com.codinginflow.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)//defines what table we want to have in our database. update the changes
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    //this method will be executed the first time we open the database, not everytime we start the app
    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,//this TaskDatabase will not actually be instantiated when this argument is injected into the constructor,
        //// instead it instantiates when we call database.get() on it (like below)
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {//Inject has 2 effects: 1. Tell dagger how to create an instance of this class/ 2. Tell dagger to pass the necessary dependencies if we define sth inside this constructor
        //Inject is the same as Provide, they tell dagger how to create an instance and what dependencies to pass
        //The reason we used Provides in AppModule was because we didn't own the classes - code from library

        override fun onCreate(db: SupportSQLiteDatabase) {//is executed later after build() in AppModule has finished
            super.onCreate(db)

            //dp operations
            val dao = database.get().taskDao()

            //need a dao, and in order to get a dao, we need an instant of task database, but our task database need a callback to get an instructor -> circular dependency - task database needs a callback, and the callback needs database
            /*this way, dagger will not try to instantiate the database when the callback is created bc then we will have circular dependency. Instead we instantiate the database when this onCreate method is executed, which happens after the build method has finished.
            , so we can get our database into the call back*/

            //Coroutine is a light thread
            //GlobalScope means that it will run as long as the app is running
            applicationScope.launch {
                dao.insert(Task("Wash the dishes"))
                dao.insert(Task("Do the laundry"))
                dao.insert(Task("Buy groceries", important = true))
                dao.insert(Task("Prepare food", completed = true))
                dao.insert(Task("Call mom"))
                dao.insert(Task("Visit grandma", completed = true))
                dao.insert(Task("Repair my bike"))
                dao.insert(Task("Call Elon Musk"))
            }
        }
    }
}