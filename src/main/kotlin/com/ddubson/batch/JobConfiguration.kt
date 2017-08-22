package com.ddubson.batch

import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.gateway.GatewayProxyFactoryBean
import org.springframework.integration.stream.CharacterStreamWritingMessageHandler

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory) : ApplicationContextAware {
    private lateinit var applicationContext: ApplicationContext

    @Bean
    fun itemReader(): ListItemReader<String> {
        return ListItemReader((0..1000).map { i -> i.toString() })
    }

    @Bean
    fun itemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            items.forEach { it -> println(">> $it") }
        }
    }

    @Bean
    fun jobExecutionListener(): Any {
        return proxyFactoryBean(JobExecutionListener::class.java)
    }

    @Bean
    fun chunkListener(): Any {
        return proxyFactoryBean(ChunkListener::class.java)
    }

    @Bean
    fun events(): DirectChannel {
        return DirectChannel()
    }

    @Bean
    @ServiceActivator(inputChannel = "events")
    fun logger(): CharacterStreamWritingMessageHandler {
        return CharacterStreamWritingMessageHandler.stdout()
    }

    @Bean
    fun step1(): Step {
        return stepBuilderFactory.get("step1")
                .chunk<String, String>(100)
                .reader(itemReader())
                .writer(itemWriter())
                .listener(chunkListener() as ChunkListener)
                .build()
    }

    @Bean
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(step1())
                .listener(jobExecutionListener() as JobExecutionListener)
                .build()
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    private fun proxyFactoryBean(cls: Class<*>): Any {
        val proxyFactoryBean = GatewayProxyFactoryBean(cls)
        proxyFactoryBean.setDefaultRequestChannel(events())
        proxyFactoryBean.setBeanFactory(applicationContext)
        return proxyFactoryBean.`object`
    }
}
