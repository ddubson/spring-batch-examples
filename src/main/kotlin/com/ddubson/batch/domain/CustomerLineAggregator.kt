package com.ddubson.batch.domain

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.batch.item.file.transform.LineAggregator

class CustomerLineAggregator : LineAggregator<Customer> {
    private val objectMapper = ObjectMapper()

    override fun aggregate(item: Customer?): String {
        return objectMapper.writeValueAsString(item)
    }
}