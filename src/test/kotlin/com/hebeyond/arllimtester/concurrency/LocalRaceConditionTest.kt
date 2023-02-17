package com.hebeyond.arllimtester.concurrency

import org.junit.jupiter.api.Test

class LocalRaceConditionTest {

    @Test
    fun simpleRaceConditionTest() {
        println(raceCondition())
    }
    private fun raceCondition(): Boolean {
        var counter = 0
        val threads = (1..100).map {
            Thread {
                for (i in 1..1000) {
                    counter++
                }
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        println(counter)
        return counter == 100000
    }
}
