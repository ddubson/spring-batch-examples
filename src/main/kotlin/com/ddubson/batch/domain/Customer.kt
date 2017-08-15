package com.ddubson.batch.domain

import org.springframework.batch.item.ResourceAware
import org.springframework.core.io.Resource
import java.util.*

data class Customer(val id: Long,
                    val firstName: String,
                    val lastName: String,
                    val birthdate: Date): ResourceAware {
    private var resource: Resource? = null

    override fun setResource(resource: Resource?) {
        this.resource = resource
    }
}