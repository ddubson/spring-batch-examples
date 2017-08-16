package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory) {
    @Bean
    @StepScope
    fun itemReader(): ListItemReader<Int> {
        return ListItemReader((0..100).toList())
    }

    @Bean
    fun itemWriter(): ItemWriter<Int> {
        return ItemWriter<Int> { items ->
            println("Writing ${items.size} items.")
            items.forEach { item -> println(">> $item") }
        }
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<Int,Int>(10)
                .reader(itemReader())
                .writer(itemWriter())
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(step1())
                .build()
    }
}