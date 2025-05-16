package com.gdghufs.nabi.utils // Replace with your actual package name

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtil {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getCurrentDateString(): String {
        return LocalDate.now().format(formatter)
    }

    fun formatDate(date: LocalDate): String {
        return date.format(formatter)
    }
}