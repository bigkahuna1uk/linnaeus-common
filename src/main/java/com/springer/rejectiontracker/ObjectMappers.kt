@file:JvmName("ObjectMappers")

package com.springer.rejectionreport

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


val objectMapper = jacksonObjectMapper().apply {
    registerModule(JavaTimeModule())
    this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    this.disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
}
