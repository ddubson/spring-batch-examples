package com.ddubson.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FlowLastConfiguration {
    @Autowired
    var jobBuilderFactory: JobBuilderFactory? = null

    @Autowired
    var stepBuilderFactory: StepBuilderFactory? = null

    @Bean
    open fun myStep(): Step {
        return stepBuilderFactory!!.get("myStep")
                .tasklet { contribution, chunkContext ->
                    println("myStep was executed.")
                    RepeatStatus.FINISHED
                }.build()
    }

    @Bean
    open fun flowLastJob(@Qualifier("myStep") myStep: Step, flow: Flow): Job {
        return jobBuilderFactory!!.get("flowLastJob")
                .start(myStep())
                .on("COMPLETED").to(flow)
                .end()
                .build()
    }
}
