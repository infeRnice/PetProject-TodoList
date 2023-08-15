package com.example.yandex1.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class TodoItemsRepository {

    private val todoItems = mutableListOf<TodoItem>()
    val error: LiveData<String> get() = _error
    //Firestore
    private val db = FirebaseFirestore.getInstance()
    private val todoCollection = db.collection("todos")
    private val _error = MutableLiveData<String>()


    init {
        todoCollection.addSnapshotListener { snapshots, e ->
            if (e != null) {
                _error.postValue(e.localizedMessage ?: "Oops")
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                todoItems.clear()
                for (document in snapshots.documents) {
                    val item = document.toObject(TodoItem::class.java)
                    if (item != null) {
                        todoItems.add(item)
                    }
                }
            }
        }
    }

    fun getTodoItems(): List<TodoItem> {
        return todoItems
    }

    fun addTodoItem(item: TodoItem) {
        todoCollection.document(item.id).set(item)
    }

    fun deleteTodoItem(item: TodoItem) {
        todoCollection.document(item.id).delete()
    }

    fun updateTodoItem(updatedItem: TodoItem) {
        val index = todoItems.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            todoItems[index] = updatedItem
        }
        todoCollection.document(updatedItem.id).set(updatedItem)
    }
}