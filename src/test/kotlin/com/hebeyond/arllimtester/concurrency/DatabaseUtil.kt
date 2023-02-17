package com.hebeyond.arllimtester.concurrency

import javax.sql.DataSource
import org.springframework.boot.jdbc.DataSourceBuilder

fun getDataSource(): DataSource {
    return DataSourceBuilder.create()
        .url("jdbc:postgresql://localhost:5432/postgres")
        .username("postgres")
        .password("postgres")
        .driverClassName("org.postgresql.Driver")
        .build()
}
