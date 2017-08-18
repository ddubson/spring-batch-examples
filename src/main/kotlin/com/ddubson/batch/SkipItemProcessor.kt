package com.ddubson.batch

import org.springframework.batch.item.ItemProcessor

open class SkipItemProcessor : ItemProcessor<String, String> {
    var skip = false
    private var attemptCount = 0

    override fun process(item: String): String {
        println("Processing item $item")

        return if (skip && item == "42") {
            attemptCount++

            println("Processing of an item $item failed")
            throw CustomRetryableException("Process failed. Attempt $attemptCount")
        } else {
            (item.toInt() * -1).toString()
        }
    }
}