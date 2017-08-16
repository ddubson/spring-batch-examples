package com.ddubson.batch.domain

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet


class CustomerRowMapper : RowMapper<Customer> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Customer {
        return Customer(rs.getLong("id"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getDate("birthdate"))
    }
}