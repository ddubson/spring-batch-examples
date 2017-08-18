package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory) {
    @Bean
    @StepScope
    fun reader(): ListItemReader<String> {
        return ListItemReader((0..100).map { i -> i.toString() })
    }

    @Bean
    fun taskExecutor():ThreadPoolTaskExecutor {
        val exec = ThreadPoolTaskExecutor()
        exec.corePoolSize = 5
        exec.maxPoolSize = 10
        exec.threadNamePrefix = "dimas-thread-"
        return exec
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<String, String>(10)
                .reader(reader())
                .processor { item ->
                    println("[${Thread.currentThread().name}]:: Processing $item.")
                    item.toUpperCase()
                }
                .writer{ items ->
                    items.forEach { i -> println("[${Thread.currentThread().name}]:: Writing $i.") }
                }
                .faultTolerant()
                .taskExecutor(taskExecutor())
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(step1())
                .build()
    }
}