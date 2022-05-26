package com.codinginflow.mvvmtodo.ui

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.codinginflow.mvvmtodo.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController//create a property for nav controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment//the setup we have to do if we want to access the nav controller in activities onCreate method
        navController = navHostFragment.findNavController()

        setupActionBarWithNavController(navController)//nav controller can handle everything for us
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp() //take care of when we click the up button, we will go back into the backstack, if this return false, to let the system decide what to do with this button
    }
}

//we needed to create additional constants for our successful add and edit. They are in the main activity because this is where the default fragments (result_canceled and result_ok) and we want to use these for diff screens
const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER//ignore this default value = 1
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1//+1 to get the next one