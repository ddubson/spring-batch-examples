package com.ddubson.batch

import org.springframework.batch.item.ItemWriter

open class SkipItemWriter : ItemWriter<String> {
    private var attemptCount = 0
    var skip = false

    override fun write(items: MutableList<out String>) {
        items.forEach { it ->
            println("Writing item $it")

            if (skip && it == "-84") {
                attemptCount++

                println("Writing item $it failed")
                throw CustomRetryableException("Write failed. Attempt $attemptCount")
            } else {
                println(it)
            }
        }
    }
}