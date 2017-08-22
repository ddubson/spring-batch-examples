package com.ddubson.batch

import org.springframework.batch.core.launch.JobOperator
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledLauncher(val jobOperator: JobOperator) {
    @Scheduled(fixedDelay = 5000L)
    fun runJob() {
        jobOperator.startNextInstance("job")
    }
}