package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.integration.launch.JobLaunchingMessageHandler
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory,
                       val jobRegistry: JobRegistry,
                       val jobLauncher: JobLauncher) {
    @Bean
    @ServiceActivator(inputChannel = "requests", outputChannel = "replies")
    fun jobLaunchingMessageHandler(): JobLaunchingMessageHandler = JobLaunchingMessageHandler(jobLauncher)

    @Bean
    fun requests(): DirectChannel = DirectChannel()

    @Bean
    fun replies(): DirectChannel = DirectChannel()

    @Bean
    @StepScope
    fun tasklet(@Value("#{jobParameters['name']}") name: String?): Tasklet {
        return Tasklet { _, _ ->
            println("The job ran for $name")
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .incrementer(RunIdIncrementer())
                .start(stepBuilderFactory.get("step1").tasklet(tasklet(null)).build())
                .build()
    }
}
