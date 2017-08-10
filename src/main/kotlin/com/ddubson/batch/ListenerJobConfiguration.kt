package com.ddubson.batch

import com.ddubson.batch.listeners.ChunkListener
import com.ddubson.batch.listeners.JobListener
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ListenerJobConfiguration {
    @Autowired
    var jobBuilderFactory: JobBuilderFactory? = null

    @Autowired
    var stepBuilderFactory: StepBuilderFactory? = null

    @Bean
    open fun reader(): ItemReader<String> {
        return ListItemReader(listOf("one", "two", "three"))
    }

    @Bean
    open fun writer(): ItemWriter<String> {
        return ItemWriter { items: List<String> ->
            items.forEach { i ->
                println("Writing item $i")
            }
        }
    }

    @Bean
    open fun step1(): Step {
        return stepBuilderFactory!!.get("step1")
                .chunk<String,String>(2)
                .faultTolerant()
                .listener(ChunkListener())
                .reader(reader())
                .writer(writer())
                .build()
    }

    @Bean
    open fun job(): Job {
        return jobBuilderFactory!!.get("job")
                .start(step1())
                .listener(JobListener())
                .build()
    }
}