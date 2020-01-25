package com.iorlova.diploma.UI.ReadingGoal

class CounterReadingGoal(startPage: Int, readGoal: Int): BaseReadingGoal() {
    override var goalId = 1
    private var currentPage: Int = startPage
    private val startPage: Int = startPage
    private val pageGoal: Int = readGoal

    override fun update(pageCount: Int) {
        currentPage = pageCount
    }
    override fun isTriggered(): Boolean {
        return currentPage == startPage + pageGoal
    }

    override fun alert():String {
        return "read $pageGoal pages"
    }
}