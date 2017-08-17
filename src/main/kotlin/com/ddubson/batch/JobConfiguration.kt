package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory) {

    @Bean
    @StepScope
    fun restartTasklet(): Tasklet {
        return Tasklet { _, chunkContext ->
            val stepExecContext = chunkContext.stepContext.stepExecutionContext
            if (stepExecContext.containsKey("ran")) {
                println("This time we'll let it go.")
                RepeatStatus.FINISHED
            } else {
                println("I don't think so...")
                chunkContext.stepContext.stepExecution.executionContext.put("ran", true)
                throw RuntimeException("Not this time...")
            }
        }
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .tasklet(restartTasklet())
                .build()
    }

    @Bean
    fun step2(): Step {
        return stepBuilderFactory.get("step2")
                .tasklet(restartTasklet())
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(step1())
                .next(step2())
                .build()
    }
}