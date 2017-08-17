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
import org.springframework.batch.item.xml.StaxEventItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.oxm.xstream.XStreamMarshaller
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
    fun itemWriter(): StaxEventItemWriter<Customer> {
        val marshaller = XStreamMarshaller()
        marshaller.setAliases(mapOf(Pair("customer", Customer::class.java)))

        val writer = StaxEventItemWriter<Customer>()
        writer.rootTagName = "customers"
        writer.setMarshaller(marshaller)

        val outputPath = File.createTempFile("customerOutput", ".xml").absolutePath
        println(">> Output path: " + outputPath)
        writer.setResource(FileSystemResource(outputPath))
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
        return jobBuilderFactory.get("writeToXMLFile")
                .start(step1())
                .build()
    }
}