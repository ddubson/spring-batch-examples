package com.ddubson.batch

import org.springframework.batch.core.launch.JobOperator
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.*

@RestController
class JobLaunchingController(val jobOperator: JobOperator) {
    @RequestMapping(value = "/", method = arrayOf(RequestMethod.POST))
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun launch(@RequestParam("name") name: String) {
        jobOperator.start("job", "name=$name")
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(OK)
    fun stop(@PathVariable("id") id: Long) {
        jobOperator.stop(id)
    }
} 