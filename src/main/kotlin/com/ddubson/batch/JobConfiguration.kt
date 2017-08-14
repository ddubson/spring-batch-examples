package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory) {
    @Bean
    fun statelessItemReader(): StatelessItemReader {
        return StatelessItemReader(listOf("Foo", "Bar", "Baz").iterator())
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<String,String>(3)
                .reader(statelessItemReader())
                .writer({ list ->
                    list.forEach { i -> println("curItem = $i") }
                })
                .build()
    }

    @Bean
    fun interfacesJob(): Job {
        return jobBuilderFactory
                .get("interfacesJob")
                .start(step1())
                .build()
    }
}