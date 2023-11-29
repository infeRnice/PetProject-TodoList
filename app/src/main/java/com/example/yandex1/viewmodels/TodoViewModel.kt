package com.example.yandex1.viewmodels

import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.yandex1.models.TodoItem
import com.example.yandex1.models.TodoItemsRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class TodoViewModel(private val repository: TodoItemsRepository) : ViewModel() {

    // LiveData для списка задач
    private val _todoItems = MutableLiveData<List<TodoItem>>()
    val todoItems: LiveData<List<TodoItem>> = repository.getTodoItems()


    private val _selectedTodoId = MutableLiveData<String>()
    val selectedTodoId: LiveData<String> get() = _selectedTodoId
    private val _showSnackbarEvent = MutableLiveData<String>()
    val showSnackbarEvent: LiveData<String> get() = _showSnackbarEvent
    val error: LiveData<String> = repository.error


    fun getGoogleSignInIntent(): Intent {
        return repository.getGoogleSignInIntent()
    }

    fun syncDataWithFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncDataWithFirestore()
            refreshTodoItems()
        }
    }

    fun postError() {
        _showSnackbarEvent.postValue("Sign-in failed")
    }

    fun onTodoItemClicked(id: String) {
        _selectedTodoId.value = id
    }

    fun getTodoItem(id: String): TodoItem? {
        return _todoItems.value?.find { it.id == id }
    }

    init {
        refreshTodoItems()
    }

    fun addTodoItem(item: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addTodoItem(item)
                refreshTodoItems() //update todo list
            } catch (e:Exception) {
                _showSnackbarEvent.postValue("Failed to add item: ${e.message}")

            }
        }
    }

    fun updateTodoItem(item: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedItem = item.copy(changeDate = Date())
                repository.updateTodoItem(updatedItem)
                refreshTodoItems()
            } catch (e: Exception) {
                _showSnackbarEvent.postValue("Failed to update item: ${e.message}")
            }
        }
    }

    fun deleteTodoItem(item: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteTodoItem(item)
                refreshTodoItems()
            } catch (e: Exception) {
                _showSnackbarEvent.postValue("Failed to delete item: ${e.message}")
            }
        }
    }

    fun onTodoCheckedChange(id: String, isChecked: Boolean) {
        viewModelScope.launch {
            val newTodoItems = _todoItems.value?.map { todoItem ->
                if (todoItem.id == id) todoItem.copy(isDone = isChecked)
                else todoItem
            }
            _todoItems.value = newTodoItems.orEmpty()
            if (newTodoItems != null) {
                repository.updateTodoItem(newTodoItems.first { it.id == id })
            }
        }
    }

    private fun refreshTodoItems() {
        viewModelScope.launch(Dispatchers.IO) {
            _todoItems.postValue(repository.getTodoItems().value)
        }
    }

    class Factory @Inject constructor(private val repository: TodoItemsRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TodoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.onCleared()
    }
}
