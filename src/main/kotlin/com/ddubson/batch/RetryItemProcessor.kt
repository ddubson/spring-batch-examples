package com.ddubson.batch

import org.springframework.batch.item.ItemProcessor

open class RetryItemProcessor : ItemProcessor<String, String> {
    var retry = false
    private var attemptCount = 0

    override fun process(item: String): String {
        println("Processing item $item")

        return if(retry && item == "42") {
            attemptCount++

            if(attemptCount >= 5) {
                println("Success!")
                retry = false
                (item.toInt() * -1).toString()
            } else {
                println("Processing of an item $item failed")
                throw CustomRetryableException("Process failed. Attempt $attemptCount")
            }
        } else {
            (item.toInt() * -1).toString()
        }
    }
}