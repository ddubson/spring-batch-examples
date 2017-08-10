package com.ddubson.batch.listeners

import org.springframework.batch.core.annotation.AfterChunk
import org.springframework.batch.core.annotation.BeforeChunk
import org.springframework.batch.core.scope.context.ChunkContext

class ChunkListener {
    @BeforeChunk
    fun beforeChunk(context:ChunkContext): Unit {
        println(">> before the chunk")
    }

    @AfterChunk
    fun afterChunk(context: ChunkContext): Unit {
        println(">> after the chunk")
    }
}