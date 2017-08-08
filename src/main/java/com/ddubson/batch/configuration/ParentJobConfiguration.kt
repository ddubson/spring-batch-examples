package com.ddubson.batch.configuration

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.JobStepBuilder
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ParentJobConfiguration {
    @Autowired
    var jobBuilderFactory: JobBuilderFactory? = null

    @Autowired
    var stepBuilderFactory: StepBuilderFactory? = null

    @Autowired
    var childJob: Job? = null

    @Autowired
    var jobLauncher: JobLauncher? = null

    @Bean
    open fun step1(): Step {
        return stepBuilderFactory!!.get("step1")
                .tasklet { _, _ ->
                    println(">> This is step 1")
                    RepeatStatus.FINISHED
                }.build()
    }

    @Bean
    open fun parentJob(jobRepository: JobRepository, transactionManager: PlatformTransactionManager?): Job {
        val childJobStep = JobStepBuilder(StepBuilder("childJobStep"))
                .job(childJob)
                .launcher(jobLauncher)
                .repository(jobRepository)
                .transactionManager(transactionManager)
                .build()

        return jobBuilderFactory!!.get("parentJob").start(step1()).next(childJobStep).build()
    }
}