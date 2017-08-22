package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor
import org.springframework.batch.core.converter.DefaultJobParametersConverter
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.batch.core.launch.support.SimpleJobOperator
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import java.text.SimpleDateFormat
import java.util.*

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory,
                       val jobRegistry: JobRegistry): DefaultBatchConfigurer(), ApplicationContextAware {
    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
      this.applicationContext = applicationContext
    }

    @Bean
    fun jobRegistrar(): JobRegistryBeanPostProcessor {
        val registrar = JobRegistryBeanPostProcessor()
        registrar.setJobRegistry(jobRegistry)
        registrar.setBeanFactory(applicationContext.autowireCapableBeanFactory)
        registrar.afterPropertiesSet()
        return registrar
    }

    @Bean
    fun jobOperator(): JobOperator {
        val jobOperator = SimpleJobOperator()
        jobOperator.setJobLauncher(jobLauncher)
        jobOperator.setJobParametersConverter(DefaultJobParametersConverter())
        jobOperator.setJobRegistry(jobRegistry)
        jobOperator.setJobExplorer(jobExplorer)
        jobOperator.setJobRepository(jobRepository)
        jobOperator.afterPropertiesSet()

        return jobOperator
    }

    @Bean
    @StepScope
    fun tasklet(): Tasklet {
        return Tasklet { _, _->
            val formatter = SimpleDateFormat("hh:mm:ss")
            println(">> I was run at ${formatter.format(Date())}")
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .incrementer(RunIdIncrementer())
                .start(stepBuilderFactory.get("step1").tasklet(tasklet()).build())
                .build()
    }

    override fun getJobLauncher(): JobLauncher {
        val jobLauncher = SimpleJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.setTaskExecutor(SimpleAsyncTaskExecutor())
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }
}
