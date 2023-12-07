package com.example.yandex1.viewmodels

import android.content.Intent
import androidx.lifecycle.*
import com.example.yandex1.models.TodoItem
import com.example.yandex1.models.TodoItemsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class TodoViewModel(private val repository: TodoItemsRepository) : ViewModel() {

    // LiveData для списка задач
    //private val _todoItems = MutableLiveData<List<TodoItem>>()
    val todoItems: LiveData<List<TodoItem>> = repository.getTodoItems()
    val error: LiveData<String> = repository.error

    private val _selectedTodoId = MutableLiveData<String?>()
    val selectedTodoId: LiveData<String?> get() = _selectedTodoId

    private val _showSnackbarEvent = MutableLiveData<String>()
    val showSnackbarEvent: LiveData<String> get() = _showSnackbarEvent

    fun getGoogleSignInIntent(): Intent {
        return repository.getGoogleSignInIntent()
    }

    fun syncDataWithFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.syncDataWithFirestore()
            } catch (e: Exception) {
                _showSnackbarEvent.postValue("Failed to sync: ${e.message}")
            }
        }
    }

    fun postError() {
        _showSnackbarEvent.postValue("Sign-in failed")
    }

    fun onTodoItemClicked(id: String) {
        _selectedTodoId.value = id
    }

    fun onTodoItemNavigated() {
        this._selectedTodoId.value = null
    }
    fun getTodoItem(id: String): LiveData<TodoItem?> {
        return repository.getTodoItemById(id)
    }

    fun addTodoItem(item: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addTodoItem(item)

            } catch (e: Exception) {
                _showSnackbarEvent.postValue("Failed to add item: ${e.message}")

            }
        }
    }

    fun updateTodoItem(item: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedItem = item.copy(changeDate = Date())
                repository.updateTodoItem(updatedItem)
//
            } catch (e: Exception) {
                _showSnackbarEvent.postValue("Failed to update item: ${e.message}")
            }
        }
    }

    fun deleteTodoItem(item: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteTodoItem(item)
//
            } catch (e: Exception) {
                _showSnackbarEvent.postValue("Failed to delete item: ${e.message}")
            }
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
