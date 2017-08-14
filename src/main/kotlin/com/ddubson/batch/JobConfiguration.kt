package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.xml.StaxEventItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.oxm.xstream.XStreamMarshaller

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory) {
    @Bean
    fun customerItemReader(): StaxEventItemReader<Customer> {
        val unmarshaller = XStreamMarshaller()
        val map = mutableMapOf<String, Class<Customer>>(Pair("customer", Customer::class.java))
        unmarshaller.setAliases(map)

        val reader = StaxEventItemReader<Customer>()
        reader.setResource(ClassPathResource("/customer.xml"))
        reader.setFragmentRootElementName("customer")
        reader.setUnmarshaller(unmarshaller)

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