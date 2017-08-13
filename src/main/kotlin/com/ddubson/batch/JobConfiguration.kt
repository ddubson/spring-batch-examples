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
    @StepScope
    fun helloWorldTasklet(@Value("#{jobParameters['message']}") message: String?): Tasklet {
        return Tasklet { _, _ ->
            println(message)
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1").tasklet(helloWorldTasklet(null)).build()
    }

    @Bean
    fun job1(): Job {
        return jobBuilderFactory.get("jobParametersJob").start(step1()).build()
    }
}