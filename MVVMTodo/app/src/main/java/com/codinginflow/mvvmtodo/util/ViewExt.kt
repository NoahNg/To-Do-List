package com.codinginflow.mvvmtodo.util

import androidx.appcompat.widget.SearchView

//contains extension functions for different views
inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true //does not do anything bc this only happens when we click the submit button, but we dont need this functionality in our app
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}