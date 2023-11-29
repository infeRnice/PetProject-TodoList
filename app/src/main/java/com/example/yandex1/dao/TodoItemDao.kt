package com.example.yandex1.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.yandex1.models.TodoItem

@Dao
interface TodoItemDao {

    @Query("SELECT * FROM TodoItem")
    fun getAllTodoItem(): LiveData<List<TodoItem>>

    @Query("SELECT * FROM TodoItem")
    fun getAllTodoItemSynchronous(): List<TodoItem>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTodoItem(todoItem: TodoItem)

    @Update
    fun updateTodoItem(todoItem: TodoItem)

    @Query("DELETE FROM TodoItem WHERE id = :id")
    fun deleteTodoItem(id: String)

    @Query("SELECT * FROM TodoItem WHERE id = :id")
    fun getById(id: String): TodoItem

}