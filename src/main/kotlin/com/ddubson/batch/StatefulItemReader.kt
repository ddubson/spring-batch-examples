package com.ddubson.batch

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStreamReader

open class StatefulItemReader(val items: List<String>) : ItemStreamReader<String> {
    private var curIndex = 0
    private var restart = false

    override fun open(executionContext: ExecutionContext) {
        println("[OPEN]")
        if (executionContext.containsKey("curIndex")) {
            curIndex = executionContext.getInt("curIndex")
            restart = true
        } else {
            curIndex = 0
            executionContext.put("curIndex", curIndex)
        }
    }

    override fun close() {
        println("[CLOSE]")
    }

    override fun update(executionContext: ExecutionContext) {
        println("[UPDATE]")
        executionContext.put("curIndex", curIndex)
    }

    override fun read(): String {
        println("[READ]")
        var item: String = ""

        if (curIndex < items.size) {
            item = items.get(curIndex)
            this.curIndex++
        }

        if (curIndex == 42 && !restart) {
            throw RuntimeException("The Answer to the Ultimate Question of Life!")
        }

        return item
    }
}