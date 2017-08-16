package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import com.ddubson.batch.domain.CustomerFieldSetMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import javax.sql.DataSource

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory,
                       val dataSource: DataSource) {
    @Bean
    fun itemReader(): FlatFileItemReader<Customer> {
        val reader = FlatFileItemReader<Customer>()
        reader.setLinesToSkip(1)
        reader.setResource(ClassPathResource("/customer.csv"))

        val customerLineMapper = DefaultLineMapper<Customer>()
        val tokenizer = DelimitedLineTokenizer()
        tokenizer.setNames(arrayOf("id", "firstName", "lastName", "birthdate"))

        customerLineMapper.setLineTokenizer(tokenizer)
        customerLineMapper.setFieldSetMapper(CustomerFieldSetMapper())
        customerLineMapper.afterPropertiesSet()

        reader.setLineMapper(customerLineMapper)

        return reader
    }

    @Bean
    fun itemWriter(): JdbcBatchItemWriter<Customer> {
        val itemWriter = JdbcBatchItemWriter<Customer>()

        itemWriter.setDataSource(dataSource)
        itemWriter.setSql("INSERT INTO CUSTOMER VALUES (:id, :firstName, :lastName, :birthdate)")
        itemWriter.setItemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider<Customer>())
        itemWriter.afterPropertiesSet()

        return itemWriter
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
        return jobBuilderFactory.get("insertIntoDBJob")
                .start(step1())
                .build()
    }
}