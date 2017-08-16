package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import com.ddubson.batch.domain.CustomerRowMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.transform.PassThroughLineAggregator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import java.io.File
import javax.sql.DataSource

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory,
                       val dataSource: DataSource) {
    @Bean
    fun itemReader(): JdbcPagingItemReader<Customer> {
        val reader = JdbcPagingItemReader<Customer>()

        reader.setDataSource(dataSource)
        reader.setFetchSize(10)
        reader.setRowMapper(CustomerRowMapper())

        val queryProvider = MySqlPagingQueryProvider()
        queryProvider.setSelectClause("id, firstName, lastName, birthdate")
        queryProvider.setFromClause("from customer")

        queryProvider.sortKeys = mapOf(Pair("id", Order.ASCENDING))
        reader.setQueryProvider(queryProvider)

        return reader
    }

    @Bean
    fun itemWriter(): FlatFileItemWriter<Customer> {
        val writer = FlatFileItemWriter<Customer>()
        writer.setLineAggregator(PassThroughLineAggregator<Customer>())

        val customerOut = File.createTempFile("customerOutput", ".out").absolutePath
        println(">> Output path $customerOut")
        writer.setResource(FileSystemResource(customerOut))
        writer.afterPropertiesSet()

        return writer
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<Customer, Customer>(10)
                .reader(itemReader())
                .writer(itemWriter())
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("writeToFlatFileJob")
                .start(step1())
                .build()
    }
}