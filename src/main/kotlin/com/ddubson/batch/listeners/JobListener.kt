package com.ddubson.batch.listeners

import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener

class JobListener : JobExecutionListener {
    override fun beforeJob(jobExecution: JobExecution?) {
        val jobName = jobExecution!!.jobInstance.jobName
        println("BeforeJob: $jobName")
    }

    override fun afterJob(jobExecution: JobExecution?) {
        val jobName = jobExecution!!.jobInstance.jobName
        println("AfterJob: $jobName")
    }
}