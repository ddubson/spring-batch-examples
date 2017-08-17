package com.ddubson.batch

import com.ddubson.batch.domain.Customer
import org.springframework.batch.item.ItemProcessor

class UppercaseItemProcessor : ItemProcessor<Customer, Customer> {
    override fun process(item: Customer): Customer {
        return Customer(item.id, item.firstName.toUpperCase(), item.lastName.toUpperCase(), item.birthdate)
    }
}