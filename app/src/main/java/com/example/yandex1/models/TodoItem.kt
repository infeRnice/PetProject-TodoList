package com.example.yandex1.models

import java.util.Date

data class TodoItem(
    val id: String = "",
    val text: String = "",
    val importance: Importance = Importance.NORMAL,
    val deadline: Date? = null,
    val isDone: Boolean = false,
    val creationDate: Date = Date(),
    val changeDate: Date? = null
)

enum class Importance {
    LOW, NORMAL, HIGH
}
