package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import com.ddubson.batch.domain.CustomerRowMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import javax.sql.DataSource

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory,
                       val dataSource: DataSource) {
    private val customerTableName = "customer"
    private val newCustomerTableName = "new_customer"

    @Bean
    fun pagingItemReader(): JdbcPagingItemReader<Customer> {
        val reader = JdbcPagingItemReader<Customer>()
        reader.setDataSource(dataSource)
        reader.setFetchSize(1000)
        reader.setRowMapper(CustomerRowMapper())

        val queryProv = MySqlPagingQueryProvider()
        queryProv.setSelectClause("id, firstName, lastName, birthdate")
        queryProv.setFromClause("from $customerTableName")
        queryProv.sortKeys = mapOf(Pair("id", Order.ASCENDING))

        reader.setQueryProvider(queryProv)
        return reader
    }

    @Bean
    fun itemProcessor(): ItemProcessor<Customer, Customer> {
        return ItemProcessor { item ->
            Customer(item.id,
                    item.firstName.toUpperCase(),
                    item.lastName.toUpperCase(),
                    item.birthdate)
        }
    }

    @Bean
    fun customerItemWriter(): JdbcBatchItemWriter<Customer> {
        val writer = JdbcBatchItemWriter<Customer>()
        writer.setDataSource(dataSource)
        writer.setSql("INSERT INTO $newCustomerTableName VALUES (:id, :firstName, :lastName, :birthdate);")
        writer.setItemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
        writer.afterPropertiesSet()

        return writer
    }

    @Bean
    fun taskExecutor(): ThreadPoolTaskExecutor {
        val exec = ThreadPoolTaskExecutor()
        exec.corePoolSize = 5
        exec.maxPoolSize = 10
        exec.threadNamePrefix = "dimas-thread-"
        return exec
    }

    @Bean
    fun step(): TaskletStep {
        return stepBuilderFactory.get("step1")
                .chunk<Customer, Customer>(1000)
                .reader(pagingItemReader())
                .processor(itemProcessor())
                .writer(customerItemWriter())
                .faultTolerant()
                .build()
    }

    @Bean
    @Profile("master")
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(step())
                .build()
    }
}
