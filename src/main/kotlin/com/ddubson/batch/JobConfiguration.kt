package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.support.ListItemReader
import org.springframework.beans.factory.annotation.Value
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
    fun processor(@Value("#{jobParameters['skip']}") skip: String?): SkipItemProcessor {
        val proc = SkipItemProcessor()
        proc.skip = !skip.isNullOrBlank() && skip!!.contains(skip) && skip.contentEquals("processor")
        return proc
    }

    @Bean
    @StepScope
    fun writer(@Value("#{jobParameters['skip']}") skip: String?): SkipItemWriter {
        val writer = SkipItemWriter()
        writer.skip = !skip.isNullOrBlank() && skip!!.contains(skip) && skip.contentEquals("writer")
        return writer
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<String, String>(10)
                .reader(reader())
                .processor(processor(""))
                .writer(writer(""))
                .faultTolerant()
                .skip(CustomRetryableException::class.java)
                .skipLimit(15)
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(step1())
                .build()
    }
}