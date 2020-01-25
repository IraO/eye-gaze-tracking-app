package com.iorlova.diploma.UI.ReadingGoal

open class BaseReadingGoal {
    open var goalId = -1

    open fun update(pageCount: Int){}

    open fun isTriggered(): Boolean {
        return false
    }

    open fun alert(): String {
        return "Base class"
    }
}