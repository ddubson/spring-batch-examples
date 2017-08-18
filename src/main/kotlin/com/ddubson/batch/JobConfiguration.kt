package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import com.ddubson.batch.domain.CustomerRowMapper
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.partition.PartitionHandler
import org.springframework.batch.integration.partition.BeanFactoryStepLocator
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler
import org.springframework.batch.integration.partition.StepExecutionRequestHandler
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.core.MessagingTemplate
import org.springframework.integration.scheduling.PollerMetadata
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.support.PeriodicTrigger
import javax.sql.DataSource

@Configuration
class JobConfiguration(val jobBuilderFactory: JobBuilderFactory,
                       val stepBuilderFactory: StepBuilderFactory,
                       val dataSource: DataSource,
                       val jobExplorer: JobExplorer): ApplicationContextAware {
    private lateinit var applicationContext: ApplicationContext
    private val customerTableName = "customer"
    private val newCustomerTableName = "new_customer"
    private val GRID_SIZE: Int = 4

    @Bean
    fun partitionHandler(messagingTemplate: MessagingTemplate?): PartitionHandler {
        val partitionHandler = MessageChannelPartitionHandler()
        partitionHandler.setStepName("slaveStep")
        partitionHandler.setGridSize(GRID_SIZE)
        partitionHandler.setMessagingOperations(messagingTemplate)
        partitionHandler.setPollInterval(5000L)
        partitionHandler.setJobExplorer(jobExplorer)
        partitionHandler.afterPropertiesSet()

        return partitionHandler
    }

    @Bean
    @Profile("slave")
    @ServiceActivator(inputChannel = "inboundRequests", outputChannel = "outboundStaging")
    fun stepExecRequestHandler(): StepExecutionRequestHandler {
        val stepExecutionRequestHandler = StepExecutionRequestHandler()

        val beanFactoryStepLocator = BeanFactoryStepLocator()
        beanFactoryStepLocator.setBeanFactory(applicationContext)
        stepExecutionRequestHandler.setStepLocator(beanFactoryStepLocator)
        stepExecutionRequestHandler.setJobExplorer(jobExplorer)

        return stepExecutionRequestHandler
    }

    @Bean(PollerMetadata.DEFAULT_POLLER)
    fun defaultPoller(): PollerMetadata {
        val poller = PollerMetadata()
        poller.trigger = PeriodicTrigger(10)
        return poller
    }

    @Bean
    fun partitioner(): ColumnRangePartitioner {
        return ColumnRangePartitioner("id", customerTableName, dataSource)
    }

    @Bean
    @StepScope
    fun pagingItemReader(@Value("#{stepExecutionContext['minValue']}") minValue: Long,
                         @Value("#{stepExecutionContext['maxValue']}") maxValue: Long): JdbcPagingItemReader<Customer> {
        println("Reading $minValue to $maxValue")
        val reader = JdbcPagingItemReader<Customer>()
        reader.setDataSource(dataSource)
        reader.setFetchSize(1000)
        reader.setRowMapper(CustomerRowMapper())

        val queryProv = MySqlPagingQueryProvider()
        queryProv.setSelectClause("id, firstName, lastName, birthdate")
        queryProv.setFromClause("from $customerTableName")
        queryProv.setWhereClause("where id >= $minValue and id < $maxValue")
        queryProv.sortKeys = mapOf(Pair("id", Order.ASCENDING))

        reader.setQueryProvider(queryProv)
        return reader
    }

    @Bean
    @StepScope
    fun itemWriter(): JdbcBatchItemWriter<Customer> {
        val writer = JdbcBatchItemWriter<Customer>()
        writer.setDataSource(dataSource)
        writer.setSql("INSERT INTO $newCustomerTableName VALUES (:id, :firstName, :lastName, :birthdate);")
        writer.setItemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
        writer.afterPropertiesSet()

        return writer
    }

    @Bean
    fun slaveStep(): Step {
        return stepBuilderFactory.get("slaveStep")
                .chunk<Customer, Customer>(10)
                .reader(pagingItemReader(0, 1))
                .writer(itemWriter())
                .build()
    }

    @Bean
    fun masterStep(): Step {
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep().name, partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(taskExecutor())
                .partitionHandler(partitionHandler(null))
                .build()
    }

    @Bean
    fun taskExecutor(): ThreadPoolTaskExecutor {
        val exec = ThreadPoolTaskExecutor()
        exec.corePoolSize = 5
        exec.maxPoolSize = 10
        exec.threadNamePrefix = "dimas-thread-"
        return exec
    }

    @Bean
    @Profile("master")
    fun job(): Job {
        return jobBuilderFactory.get("job")
                .start(masterStep())
                .build()
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}
