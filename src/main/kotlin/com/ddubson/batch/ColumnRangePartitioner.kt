package com.ddubson.batch

import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.item.ExecutionContext
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

open class ColumnRangePartitioner(private val column: String,
                             private val table: String,
                             private val dataSource: DataSource) : Partitioner {
    override fun partition(gridSize: Int): MutableMap<String, ExecutionContext> {
        val jdbcTemplate = JdbcTemplate(dataSource)
        val min = jdbcTemplate.queryForObject("SELECT MIN($column) from $table", Integer::class.java).toInt()
        val max = jdbcTemplate.queryForObject("SELECT MAX($column) from $table", Integer::class.java).toInt()
        val targetSize = (max - min) / gridSize + 1

        val result = mutableMapOf<String, ExecutionContext>()
        var number = 0
        var start = min
        var end = start + targetSize - 1

        while(start <= max) {
            val value = ExecutionContext()
            result.put("partition$number", value)

            if(end >= max) {
                end = max
            }

            value.putInt("minValue", start)
            value.putInt("maxValue", end)
            start += targetSize
            end += targetSize
            number++
        }

        return result
    }
}