package com.ddubson.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor

@Configuration
open class BatchConfiguration {
    @Autowired
    var jobBuilderFactory : JobBuilderFactory? = null;

    @Autowired
    var stepBuilderFactory : StepBuilderFactory? = null;

    @Bean
    open fun tasklet(): Tasklet = CountingTasklet()

    @Bean
    open fun flow1(): Flow {
        return FlowBuilder<Flow>("flow1")
                .start(stepBuilderFactory!!.get("step1").tasklet(tasklet()).build()).build()
    }

    @Bean
    open fun flow2(): Flow {
        return FlowBuilder<Flow>("flow2")
                .start(stepBuilderFactory!!.get("step2").tasklet(tasklet()).build())
                .next(stepBuilderFactory!!.get("step3").tasklet(tasklet()).build())
                .build()
    }

    @Bean
    open fun job(): Job {
        val flow1: Flow? = flow1()
        val flow2: Flow? = flow2()

        return jobBuilderFactory!!.get("job")
                .start(flow1)
                .split(SimpleAsyncTaskExecutor()).add(flow2)
                .end()
                .build()
    }

    class CountingTasklet : Tasklet {
        override fun execute(contribution: StepContribution?, chunkContext: ChunkContext?): RepeatStatus {
            println(String.format("%s has been executed on thread %s",
                    chunkContext!!.stepContext!!.stepName, Thread.currentThread().name))
            return RepeatStatus.FINISHED
        }
    }
}