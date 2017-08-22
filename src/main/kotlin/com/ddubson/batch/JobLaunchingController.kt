package com.ddubson.batch

import org.springframework.batch.core.launch.JobOperator
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class JobLaunchingController(val jobOperator: JobOperator) {
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.POST))
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun launch(@RequestParam("name") name: String) {
        jobOperator.start("job", "name=$name")
    }
} 