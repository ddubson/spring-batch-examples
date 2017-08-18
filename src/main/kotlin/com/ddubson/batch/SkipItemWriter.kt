package com.ddubson.batch

import org.springframework.batch.item.ItemWriter

open class SkipItemWriter : ItemWriter<String> {
    private var attemptCount = 0

    override fun write(items: MutableList<out String>) {
        items.forEach { it ->
            if (it == "-84") {
                throw CustomException("Write failed. Attempt $attemptCount")
            } else {
                println(it)
            }
        }
    }
}