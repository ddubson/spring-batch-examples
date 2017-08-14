package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory) {
    @Bean
    fun customerItemReader(): FlatFileItemReader<Customer> {
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
    fun customerItemWriter(): ItemWriter<Customer> {
        return ItemWriter<Customer> { items ->
            items.forEach { item -> println(item) }
        }
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<Customer, Customer>(10)
                .reader(customerItemReader())
                .writer(customerItemWriter())
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(step1())
                .build()
    }
}