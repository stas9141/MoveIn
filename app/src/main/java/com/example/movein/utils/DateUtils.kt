package com.example.movein.utils

import java.util.Calendar

fun getTodayString(): String {
    val calendar = Calendar.getInstance()
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val year = calendar.get(Calendar.YEAR)
    return String.format("%02d/%02d/%04d", month, day, year)
}

fun getTomorrowString(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val year = calendar.get(Calendar.YEAR)
    return String.format("%02d/%02d/%04d", month, day, year)
}

fun getNextWeekString(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.WEEK_OF_YEAR, 1)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val year = calendar.get(Calendar.YEAR)
    return String.format("%02d/%02d/%04d", month, day, year)
}
