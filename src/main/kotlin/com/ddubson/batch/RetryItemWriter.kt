package com.ddubson.batch

import org.springframework.batch.item.ItemWriter

open class RetryItemWriter: ItemWriter<String> {
    private var attemptCount = 0
    var retry = false

    override fun write(items: MutableList<out String>) {
        items.forEach { it ->
            println("Writing item $it")

            if(retry && it == "-84") {
                attemptCount++

                if(attemptCount >= 5) {
                    println("Success!")
                    retry = false
                    println(it)
                } else {
                    println("Writing item $it failed")
                    throw CustomRetryableException("Write failed. Attempt $attemptCount")
                }
            } else {
                println(it)
            }
        }
    }
}