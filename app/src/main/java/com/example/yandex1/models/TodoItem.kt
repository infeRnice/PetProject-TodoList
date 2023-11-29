package com.example.yandex1.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class TodoItem(
    @PrimaryKey val id: String = "",
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
