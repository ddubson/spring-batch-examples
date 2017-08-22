package com.ddubson.batch

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@EnableBatchProcessing
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
