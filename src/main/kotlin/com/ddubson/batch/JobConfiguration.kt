package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory) {
    @Bean
    @StepScope
    fun reader(): ListItemReader<String> {
        return ListItemReader((0..100).map { i -> i.toString() })
    }

    @Bean
    @StepScope
    fun processor(): SkipItemProcessor = SkipItemProcessor()

    @Bean
    @StepScope
    fun writer(): SkipItemWriter = SkipItemWriter()

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<String, String>(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .faultTolerant()
                .skip(CustomException::class.java)
                .skipLimit(15)
                .listener(CustomSkipListener())
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(step1())
                .build()
    }
}