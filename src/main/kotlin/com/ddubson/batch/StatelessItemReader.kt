package com.ddubson.batch

import org.springframework.batch.item.ItemReader

class StatelessItemReader(val data: Iterator<String>) : ItemReader<String> {
    override fun read(): String? {
        if(data.hasNext()) {
            return data.next()
        } else
            return null
    }
}