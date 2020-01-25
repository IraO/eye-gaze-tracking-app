package com.iorlova.diploma.UI.ReadingGoal

class TimerReadingGoal(val duration: String): BaseReadingGoal() {
    override var goalId = 0

    override fun convertValue(): Long {
        val minInMiliseconds = 60000L
        return duration.toLong() * minInMiliseconds
    }
    override fun alert(): String {
        return "read for $duration min"
    }
}