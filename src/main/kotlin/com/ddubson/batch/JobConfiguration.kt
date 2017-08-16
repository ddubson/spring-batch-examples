package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory) {
    @Bean
    @StepScope
    fun itemReader(): StatefulItemReader {
        return StatefulItemReader((1..100).map { i -> i.toString()})
    }

    @Bean
    fun customerItemWriter(): ItemWriter<String> {
        return ItemWriter<String> { items ->
            items.forEach { item -> println(">> $item") }
        }
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<String,String>(10)
                .reader(itemReader())
                .writer(customerItemWriter())
                .stream(itemReader())
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(step1())
                .build()
    }
}