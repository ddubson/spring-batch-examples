package com.ddubson.batch

import org.springframework.batch.item.ItemProcessor

open class SkipItemProcessor : ItemProcessor<String, String> {
    private var attemptCount = 0

    override fun process(item: String): String {
        return if (item == "42") {
            throw CustomException("Process failed. Attempt $attemptCount")
        } else {
            (item.toInt() * -1).toString()
        }
    }
}