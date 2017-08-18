package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.batch.core.step.item.SimpleChunkProcessor
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.integration.chunk.ChunkHandler
import org.springframework.batch.integration.chunk.ChunkMessageChannelItemWriter
import org.springframework.batch.integration.chunk.ChunkProcessorChunkHandler
import org.springframework.batch.integration.chunk.RemoteChunkHandlerFactoryBean
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.core.MessagingTemplate
import org.springframework.integration.scheduling.PollerMetadata
import org.springframework.messaging.MessageChannel
import org.springframework.scheduling.support.PeriodicTrigger

@Configuration
class IntegrationConfig {
    private val CHUNKING_REQUESTS = "chunking.requests"
    private val CHUNKING_REPLIES = "chunking.replies"

    @Bean
    @Profile("master")
    fun inboundRepliesAdapter(requestContainer: SimpleMessageListenerContainer):AmqpInboundChannelAdapter {
        val adapter = AmqpInboundChannelAdapter(requestContainer)
        adapter.outputChannel = inboundReplies()
        adapter.afterPropertiesSet()
        return adapter
    }

    @Bean
    fun chunkHandler(step1: TaskletStep): ChunkHandler<Customer> {
        val factoryBean = RemoteChunkHandlerFactoryBean<Customer>()
        factoryBean.setChunkWriter(chunkWriter())
        factoryBean.setStep(step1)
        return factoryBean.`object`
    }

    @Bean
    fun chunkWriter(): ChunkMessageChannelItemWriter<Customer> {
        val chunkWriter = ChunkMessageChannelItemWriter<Customer>()
        chunkWriter.setMessagingOperations(messageTemplate())
        chunkWriter.setReplyChannel(inboundReplies())
        chunkWriter.setMaxWaitTimeouts(10)

        return chunkWriter
    }

    @Bean
    fun messageTemplate(): MessagingTemplate {
        val messagingTemplate = MessagingTemplate(outboundRequests())
        messagingTemplate.receiveTimeout = 6000000L
        return messagingTemplate
    }

    @Bean
    @ServiceActivator(inputChannel = "outboundRequests")
    fun amqpOutboundEndpoint(template: AmqpTemplate): AmqpOutboundEndpoint {
        val endpoint = AmqpOutboundEndpoint(template)
        endpoint.setExpectReply(false)
        endpoint.outputChannel = inboundReplies()
        endpoint.setRoutingKey(CHUNKING_REQUESTS)

        return endpoint
    }

    @Bean
    @Profile("slave")
    fun inboundRequestsAdapter(listenerContainer: SimpleMessageListenerContainer): AmqpInboundChannelAdapter {
        val adapter = AmqpInboundChannelAdapter(listenerContainer)
        adapter.outputChannel = inboundRequests()
        adapter.afterPropertiesSet()

        return adapter
    }

    @Bean
    @Profile("slave")
    @ServiceActivator(inputChannel = "inboundRequests", outputChannel = "outboundReplies")
    fun chunkProcessorChunkHandler(itemProcessor: ItemProcessor<Customer, Customer>,
                                   customerItemWriter: ItemWriter<Customer>): ChunkProcessorChunkHandler<Customer> {
        val chunkProcessor = SimpleChunkProcessor<Customer, Customer>(itemProcessor, customerItemWriter)
        chunkProcessor.afterPropertiesSet()

        val chunkHandler = ChunkProcessorChunkHandler<Customer>()
        chunkHandler.setChunkProcessor(chunkProcessor)
        chunkHandler.afterPropertiesSet()

        return chunkHandler
    }

    @Bean
    @Profile("slave")
    @ServiceActivator(inputChannel = "outboundReplies")
    fun amqpOutboundEndpointReplies(template:AmqpTemplate): AmqpOutboundEndpoint {
        val endpoint = AmqpOutboundEndpoint(template)
        endpoint.setExpectReply(false)
        endpoint.setRoutingKey(CHUNKING_REPLIES)
        return endpoint
    }

    @Bean
    @Profile("slave")
    fun requestContainer(connFactory: ConnectionFactory): SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer(connFactory)
        container.setQueueNames(CHUNKING_REQUESTS)
        container.isAutoStartup = false
        return container
    }

    @Bean
    @Profile("master")
    fun replyContainer(connFactory: ConnectionFactory): SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer(connFactory)
        container.setQueueNames(CHUNKING_REPLIES)
        container.isAutoStartup = false
        return container
    }

    @Bean(PollerMetadata.DEFAULT_POLLER)
    fun defaultPoller(): PollerMetadata {
        val pol = PollerMetadata()
        pol.trigger = PeriodicTrigger(10L)
        return pol
    }

    @Bean
    fun replyQueue(): Queue = Queue(CHUNKING_REPLIES, false)

    @Bean
    fun requestQueue(): Queue = Queue(CHUNKING_REQUESTS, false)

    @Bean
    fun inboundRequests(): MessageChannel = DirectChannel()

    @Bean
    fun inboundReplies(): QueueChannel = QueueChannel()

    @Bean
    fun outboundReplies(): QueueChannel = QueueChannel()

    @Bean
    fun outboundRequests(): MessageChannel = DirectChannel()
}
