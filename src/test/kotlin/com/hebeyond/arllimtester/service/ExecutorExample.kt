package com.hebeyond.arllimtester.service

import java.lang.Thread.sleep
import java.util.concurrent.Executors
import org.junit.jupiter.api.Test

class ExecutorExample {
    private val service = Executors.newFixedThreadPool(100)

    @Test
    fun test1() {
        for (i in 1..10) {
            service.execute {
                println("Hello, world!")
                println("current thread: ${Thread.currentThread().name}")
                println("time: ${System.currentTimeMillis()}")
                sleep(1000)
            }
        }
    }
}
