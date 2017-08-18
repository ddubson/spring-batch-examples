package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.integration.async.AsyncItemProcessor
import org.springframework.batch.integration.async.AsyncItemWriter
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
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
    fun itemProcessor(): ItemProcessor<String, String> {
        return ItemProcessor { item ->
            Thread.sleep(1000)
            println("[${Thread.currentThread().name}]:: Processing $item.")
            item.toUpperCase()
        }
    }

    @Bean
    fun asyncItemProcessor(): AsyncItemProcessor<String, String> {
        val asyncProc= AsyncItemProcessor<String, String>()
        asyncProc.setDelegate(itemProcessor())
        asyncProc.setTaskExecutor(taskExecutor())
        asyncProc.afterPropertiesSet()

        return asyncProc
    }

    @Bean
    fun itemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            items.forEach { i -> println("[${Thread.currentThread().name}]:: Writing $i.") }
        }
    }

    @Bean
    fun asyncItemWriter(): AsyncItemWriter<String> {
        val asyncWriter = AsyncItemWriter<String>()
        asyncWriter.setDelegate(itemWriter())
        asyncWriter.afterPropertiesSet()
        return asyncWriter
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
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<String, String>(10)
                .reader(reader())
                .processor(asyncItemProcessor() as ItemProcessor<in String, out String>)
                .writer(asyncItemWriter() as ItemWriter<in String>)
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(step1())
                .build()
    }
}