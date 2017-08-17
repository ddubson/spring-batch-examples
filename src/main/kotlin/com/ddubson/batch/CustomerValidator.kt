package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import org.springframework.batch.item.validator.ValidationException
import org.springframework.batch.item.validator.Validator

class CustomerValidator : Validator<Customer> {
    override fun validate(value: Customer) {
        if (value.firstName.startsWith("A"))
            throw ValidationException("First names that begin with A ar invalid: ${value.toString()}")
    }
}