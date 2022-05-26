package com.codinginflow.mvvmtodo.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

enum class SortOrder { BY_NAME, BY_DATE }

data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)//this will encapsulate both of our filter preferences: hide completed state and sortOrder by pasting them as properties

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {//belongs to the whole application

    private val dataStore = context.createDataStore("user_preferences")

    val preferencesFlow = dataStore.data//Flow from preferences and preferences is from datastore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())//this emits empty preferences so that our map value will use the default values instead of crashing
            } else {
                throw exception
            }
        }
        .map { preferences -> //map takes all the values that are in this flow, and transform them in somewhere else
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name//if there is no value yet, it will use SortOrder.BY_DATE.name as the default value
            )
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: false
            FilterPreferences(sortOrder, hideCompleted)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->//so we can make changes to it
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
        }
    }

    private object PreferencesKeys {//how we can distinguish different values that we have in our data store
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }
}