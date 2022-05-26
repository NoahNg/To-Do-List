package com.codinginflow.mvvmtodo.di

import android.app.Application
import androidx.room.Room
import com.codinginflow.mvvmtodo.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module //a place where we can give dagger an instruction on how to create the dependencies that we need -> If we later need this object somewhere, dagger will automatically provide it for us
@InstallIn(ApplicationComponent::class)//this is an object that contains the dependencies that we want to use throughout our whole application
object AppModule {//object makes generating code more efficient

    @Provides//tells dagger that this is supposed to be one of the instruction functions
    @Singleton//only ever create one instance of our task_database
    fun provideDatabase(
        app: Application,
        callback: TaskDatabase.Callback
    ) = Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
        .fallbackToDestructiveMigration()//tell room what to do when we update our database schemer but didnt declare a proper migration strategy.
        // In this case, it just drops the table and creates a new one
        .addCallback(callback)
        .build()

    @Provides
    fun provideTaskDao(db: TaskDatabase) = db.taskDao()//provides the task dao object that we need to make the database operation. Need an instance of our task_database.
    //always a singleton automatically
    //later if we need a task dao, dagger will look where it can get a task dao from and since we have this provide method that has taskDao as its return type,
    //dagger will know it's this one -> need a task database -> provide Database-> need an application but dagger already knows what to do w this one

    @ApplicationScope//tell dagger that this is not just any coroutine scope, it's the application scope
    //provide coroutine scope for our app
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)//this qualifier will be visible for reflection
@Qualifier
annotation class ApplicationScope