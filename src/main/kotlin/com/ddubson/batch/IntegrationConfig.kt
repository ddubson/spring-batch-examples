package com.ddubson.batch

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.channel.NullChannel
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.core.MessagingTemplate
import org.springframework.messaging.PollableChannel

@Configuration
class IntegrationConfig {
    @Bean
    fun messagingTemplate(): MessagingTemplate {
        val messagingTemplate = MessagingTemplate(outboundRequests())
        messagingTemplate.receiveTimeout = 60000000L
        return messagingTemplate
    }

    @Bean("outboundRequests")
    fun outboundRequests(): DirectChannel {
        return DirectChannel()
    }

    @Bean
    @ServiceActivator(inputChannel = "outboundRequests")
    fun amqpOutboundEndpoint(template: AmqpTemplate): AmqpOutboundEndpoint {
        val endpoint = AmqpOutboundEndpoint(template)
        endpoint.setExpectReply(true)
        endpoint.outputChannel = inboundRequests()
        endpoint.setRoutingKey("partition.requests")
        return endpoint
    }

    @Bean
    fun requestQueue(): Queue {
        return Queue("partition.requests", false)
    }

    @Bean
    @Profile("slave")
    fun inbound(listenerContainer: SimpleMessageListenerContainer): AmqpInboundChannelAdapter {
        val adapter = AmqpInboundChannelAdapter(listenerContainer)
        adapter.outputChannel = inboundRequests()
        adapter.afterPropertiesSet()
        return adapter
    }

    @Bean
    fun container(connFactory: ConnectionFactory): SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer(connFactory)
        container.setQueueNames("partition.requests")
        container.isAutoStartup = false
        return container
    }

    @Bean
    fun outboundStaging(): PollableChannel {
        return NullChannel()
    }

    @Bean
    fun inboundRequests(): QueueChannel {
        return QueueChannel()
    }
}