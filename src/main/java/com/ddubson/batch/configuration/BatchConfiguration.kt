package com.ddubson.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class BatchConfiguration {
    @Autowired
    var jobBuilderFactory: JobBuilderFactory? = null

    @Autowired
    var stepBuilderFactory: StepBuilderFactory? = null

    @Bean
    open fun startStep(): Step {
        return stepBuilderFactory!!.get("startStep").tasklet { contribution, chunkContext ->
            println("This is the start tasklet")
            RepeatStatus.FINISHED
        }.build()
    }

    @Bean
    open fun evenStep(): Step {
        return stepBuilderFactory!!.get("evenStep").tasklet { _, _ ->
            println("This is the even tasklet")
            RepeatStatus.FINISHED
        }.build()
    }

    @Bean
    open fun oddStep(): Step {
        return stepBuilderFactory!!.get("oddStep").tasklet { _, _ ->
            println("This is the odd step")
            RepeatStatus.FINISHED
        }.build()
    }

    @Bean
    open fun decider(): JobExecutionDecider {
        return OddDecider()
    }

    @Bean
    open fun job(): Job {
        return jobBuilderFactory!!.get("job")
                .start(startStep())
                .next(decider())
                .from(decider()).on("ODD").to(oddStep())
                .from(decider()).on("EVEN").to(evenStep())
                .from(oddStep()).on("*").to(decider())
                .from(decider()).on("ODD").to(oddStep())
                .from(decider()).on("EVEN").to(evenStep())
                .end().build()
    }
}