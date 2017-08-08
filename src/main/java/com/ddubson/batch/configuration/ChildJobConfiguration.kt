package com.ddubson.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChildJobConfiguration {
    @Autowired
    var jobBuilderFactory: JobBuilderFactory? = null

    @Autowired
    var stepBuilderFactory: StepBuilderFactory? = null

    @Bean
    open fun step1a(): Step {
        return stepBuilderFactory!!.get("step1a")
                .tasklet { _, _ ->
                    println("\t>> This is step 1a")
                    RepeatStatus.FINISHED
                }.build()
    }

    @Bean
    open fun childJob(): Job {
        return jobBuilderFactory!!.get("childJob").start(step1a()).build()
    }
}