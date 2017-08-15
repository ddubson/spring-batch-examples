package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import com.ddubson.batch.domain.CustomerFieldSetMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.MultiResourceItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory,
                       @Value("classpath*:/customer*.csv") val inputFiles: Array<Resource>) {
    @Bean
    fun multiResourceItemReader(): MultiResourceItemReader<Customer> {
        val reader = MultiResourceItemReader<Customer>()
        reader.setDelegate(customerItemReader())
        reader.setResources(inputFiles)
        return reader
    }

    @Bean
    fun customerItemReader(): FlatFileItemReader<Customer> {
        val reader = FlatFileItemReader<Customer>()
        reader.setLinesToSkip(1)

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
                .reader(multiResourceItemReader())
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