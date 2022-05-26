package com.codinginflow.mvvmtodo.util

//this is an extension property. What it does: simply return the same object, but it can turn a statement into an expression
val <T> T.exhaustive: T
    get() = this