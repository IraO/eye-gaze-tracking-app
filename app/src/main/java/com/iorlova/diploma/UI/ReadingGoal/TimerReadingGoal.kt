package com.iorlova.diploma.UI.ReadingGoal

import java.util.*
import kotlin.concurrent.timer

class TimerReadingGoal(val duration: String): BaseReadingGoal() {
    override var goalId = 0
    val startTime: String = ""
    val finishTime = ""


    override fun alert(): String {
        return "Read for $duration min"
    }
}