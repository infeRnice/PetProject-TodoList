package com.example.yandex1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.yandex1.models.TodoItem
import com.example.yandex1.models.TodoItemsRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class TodoViewModel(private val repository: TodoItemsRepository) : ViewModel() {

    private val _todoItems = MutableLiveData<List<TodoItem>>()
    val todoItems: LiveData<List<TodoItem>> = _todoItems
    private val _selectedTodoId = MutableLiveData<String>()
    val selectedTodoId: LiveData<String> get() = _selectedTodoId
    private val _showSnackbarEvent = MutableLiveData<String>()
    val showSnackbarEvent: LiveData<String> get()= _showSnackbarEvent


    fun onTodoItemClicked(id: String) {
        _selectedTodoId.value = id
    }

    fun getTodoItem(id: String): TodoItem? {
        return _todoItems.value?.find { it.id == id }
    }

    init {
        repository.error.observeForever {errorMessage ->
            _showSnackbarEvent.postValue(errorMessage)
        }

        _todoItems.value = repository.getTodoItems()
    }

    fun addTodoItem(item: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTodoItem(item)
            _todoItems.postValue(repository.getTodoItems())
        }
    }

    fun updateTodoItem(item: TodoItem)  {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTodoItem(item)
            _todoItems.postValue(repository.getTodoItems())
        }
    }

    fun deleteTodoItem(item: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTodoItem(item)
            _todoItems.postValue(repository.getTodoItems())
        }
    }

    fun onTodoCheckedChange(id: String, isChecked: Boolean) {
        val newTodoItems = _todoItems.value?.map { todoItem ->
            if (todoItem.id == id) {
                todoItem.copy(isDone = isChecked).also {
                    repository.updateTodoItem(it) // Обновляем элемент в репозитории
                }
            } else {
                todoItem
            }
        }.orEmpty()

        _todoItems.value = newTodoItems
    }

    class Factory @Inject constructor(private val repository: TodoItemsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TodoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
