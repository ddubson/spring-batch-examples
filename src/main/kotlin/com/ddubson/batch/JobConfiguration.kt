package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import com.ddubson.batch.domain.CustomerRowMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory,
                       val dataSource: DataSource) {
    /** Non-thread safe **/
    //@Bean
    fun cursorItemReader(): JdbcCursorItemReader<Customer> {
        val reader = JdbcCursorItemReader<Customer>()
        reader.sql = "select id, firstName, lastName, birthdate from customer order by lastName, firstName"
        reader.dataSource = this.dataSource
        reader.setRowMapper(CustomerRowMapper())

        return reader
    }

    /** Thread-safe **/
    @Bean
    fun pagingItemReader(): JdbcPagingItemReader<Customer> {
        val reader = JdbcPagingItemReader<Customer>()
        reader.setDataSource(this.dataSource)
        reader.setFetchSize(10) // fetch size should be close to chunk size
        reader.setRowMapper(CustomerRowMapper())

        val queryProvider: MySqlPagingQueryProvider = MySqlPagingQueryProvider()
        queryProvider.setSelectClause("id, firstName, lastName, birthdate")
        queryProvider.setFromClause("from customer")
        queryProvider.sortKeys = mapOf(Pair("id", Order.ASCENDING))

        reader.setQueryProvider(queryProvider)

        return reader;
    }

    @Bean
    fun customerItemWriter(): ItemWriter<Customer> {
        return ItemWriter<Customer> { items ->
            items.forEach { i -> println(i.toString()) }
        }
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<Customer, Customer>(10)
                .reader(cursorItemReader())
                .writer(customerItemWriter())
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job1")
                .start(step1())
                .build();
    }
}