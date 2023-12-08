package com.example.yandex1.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.yandex1.models.TodoItem
import java.util.Date
import java.util.concurrent.Flow

@Dao
interface TodoItemDao {

    @Query("SELECT * FROM TodoItem")
    fun getAllTodoItem(): kotlinx.coroutines.flow.Flow<List<TodoItem>>

    @Query("SELECT * FROM TodoItem")
    fun getAllTodoItemSynchronous(): List<TodoItem>

    @Query("SELECT * FROM TodoItem WHERE id = :id")
    fun getTodoItemById(id: String): LiveData<TodoItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTodoItem(todoItem: TodoItem)

    @Update
    fun updateTodoItem(todoItem: TodoItem)

    @Query("DELETE FROM TodoItem WHERE id = :id")
    fun deleteTodoItem(id: String)

    @Query("SELECT MAX(changeDate) FROM TodoItem")
    fun getMaxChangeDate(): Date?

    @Query("SELECT * FROM TodoItem WHERE id = :id")
    fun getById(id: String): TodoItem
}