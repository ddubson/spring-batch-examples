package com.ddubson.batch.domain

import java.util.*

data class Customer(val id: Long,
                    val firstName: String,
                    val lastName: String,
                    val birthdate: Date) {
}