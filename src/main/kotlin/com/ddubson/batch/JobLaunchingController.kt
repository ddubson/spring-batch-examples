package com.ddubson.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.integration.launch.JobLaunchRequest
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.MessageChannel
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class JobLaunchingController(val requests: MessageChannel,
                             val replies: DirectChannel,
                             val job: Job) {
    @PostMapping("/")
    @ResponseStatus(ACCEPTED)
    fun launch(@RequestParam("name") name:String) {
        val jobParams = JobParametersBuilder().addString("name", name).toJobParameters()
        val launchRequest = JobLaunchRequest(job, jobParams)

        replies.subscribe { message ->
            val payload: JobExecution = message.payload as JobExecution
            println(">> ${payload.jobInstance.jobName} resulted in ${payload.status}")
        }

        requests.send(MessageBuilder.withPayload(launchRequest).setReplyChannel(replies).build())
    }
}