package com.ddubson.batch.domain

import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.FieldSet

class CustomerFieldSetMapper : FieldSetMapper<Customer> {
    override fun mapFieldSet(fieldSet: FieldSet): Customer {
        return Customer(fieldSet.readLong("id"),
                fieldSet.readString("firstName"),
                fieldSet.readString("lastName"),
                fieldSet.readDate("birthdate", "yyyy-MM-dd HH:mm:ss"))
    }
}