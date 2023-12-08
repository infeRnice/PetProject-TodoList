package com.example.yandex1.models

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import kotlin.collections.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yandex1.database.TodoDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.Date

class TodoItemsRepository(private val context: Context) {

    private val _error = MutableLiveData<String>()

    //val error: LiveData<String> get() = _error
    val error: LiveData<String> = _error

    // Использование сервиса подключения
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Инициализация обратного вызова сети
    private val networkCallback = NetworkCallbackImpl()

    //Firestore
    private val db = FirebaseFirestore.getInstance()
    private val todoCollection = db.collection("todos")
    private val auth = FirebaseAuth.getInstance()

    //Room database
    private val todoDao = TodoDatabase.getDatabase(context).todoDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            syncDataWithFirestore()
        }
        // Для мониторинга сети
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback
        )
    }

    //Синхронизация с Firestore
    suspend fun syncDataWithFirestore() = withContext(Dispatchers.IO) {
        try {
            // Получаем последнюю временную метку из Room
            val lastLocalChangeDate = todoDao.getMaxChangeDate() ?: Date(0)
            // Получаем последнюю временную метку из Firestore
            val lastRemoteChangeDate = getLastRemoteChangeDate() ?: Date(0)
            //сравниваем
            if (lastRemoteChangeDate.after(lastLocalChangeDate)) {
                fetchDataFromFirestore()
            }
        } catch (e: Exception) {
            _error.postValue("Failed to synchronize with Firestore: ${e.message}")
        }
    }

    private suspend fun getLastRemoteChangeDate(): Date? {
        val lastRemoteUpdate = db.collection("metadata").document("lastUpdate").get().await()
        return lastRemoteUpdate.getDate("timestamp")
    }

    private suspend fun updateLastChangeTimestampInFirestore() {
        val timestamp = hashMapOf("timestamp" to FieldValue.serverTimestamp())
        db.collection("metadata").document("lastUpdate").set(timestamp).await()
    }

    fun getGoogleSignInIntent(): Intent {
        //Start Google sign-in process
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1078527184911-30ia1m86q9b60a25170trsele44gml2m.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        return googleSignInClient.signInIntent
    }

    fun pushLocalChangesToFirestore() {
        CoroutineScope(Dispatchers.IO).launch {
            val localItems =
                todoDao.getAllTodoItemSynchronous()

            localItems.forEach { item ->
                try {
                    todoCollection.document(item.id).set(item).await()
                    updateLastChangeTimestampInFirestore()
                } catch (e: Exception) {
                    _error.postValue("Failed to push item to Firestore: ${e.message}")
                }
            }
        }
    }

    private suspend fun fetchDataFromFirestore() = withContext(Dispatchers.IO) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("Repository", "User authenticated: ${currentUser.uid}")
            auth.currentUser?.let { user ->
                todoCollection.whereEqualTo("userId", user.uid).get()
                    .addOnSuccessListener { querySnapshot ->
                        querySnapshot.documents.forEach { document ->
                            val todoItem = document.toObject(TodoItem::class.java)
                            todoItem?.let { item ->
                                //проверяем, существует ли запись в Room
                                val existingItem = todoDao.getTodoItemById(item.id)
                                if (existingItem == null) {
                                    //если записи нет, добавляем
                                    todoDao.insertTodoItem(item)
                                } else {
                                    //if todoItem exists then update it
                                    todoDao.updateTodoItem(item)
                                }
                            }
                        }
                    }.addOnFailureListener { e ->
                    _error.postValue("Error fetching data: ${e.localizedMessage}")
                }
                todoDao.getMaxChangeDate() ?: Date(0)
            }
        } else {
            Log.d("Repository", "User is not authenticated")
            _error.postValue("User not authenticated")
        }
    }

    fun getTodoItemById(id: String): LiveData<TodoItem?> {
        return todoDao.getTodoItemById(id)
    }

    fun getTodoItems(): LiveData<List<TodoItem>> {
        Log.d("TodoListFragment", "getTodoItems() called")
        return todoDao.getAllTodoItem().asLiveData()
    }

    suspend fun addTodoItem(item: TodoItem) = withContext(Dispatchers.IO) {
        Log.d("FirestoreAdd", "Adding todo item: ${item.id}")
        val newItem = item.copy(changeDate = Date())
        todoDao.insertTodoItem(newItem)
        todoCollection.document(newItem.id).set(newItem).await()
    }

    suspend fun deleteTodoItem(item: TodoItem) = withContext(Dispatchers.IO) {
        todoDao.deleteTodoItem(item.id)
        todoCollection.document(item.id).set(item).await()
    }

    suspend fun updateTodoItem(item: TodoItem) = withContext(Dispatchers.IO) {
        val updatedItem = item.copy(changeDate = Date())
        todoDao.updateTodoItem(updatedItem)
        todoCollection.document(updatedItem.id).set(updatedItem).await()
    }

    fun onCleared() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private inner class NetworkCallbackImpl : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            CoroutineScope(Dispatchers.IO).launch {
                syncDataWithFirestore()
            }
        }

        override fun onLost(network: Network) {
            _error.postValue("No internet connection")
        }
    }
}

/*if (user != null) {
    todoCollection.addSnapshotListener { snapshots, e ->
        if (e != null) {
            Log.e("FirestoreError", "Error fetching data: ", e)
            _error.postValue(e.localizedMessage ?: "Oops")
            return@addSnapshotListener
        }

        snapshots?.let { snapshot ->
            for (document in snapshot.documents) {
                try {
                    val item = document.toObject(TodoItem::class.java)
                    item?.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            todoDao.insertTodoItem(item)
                            Log.d(
                                "RoomData",
                                "Inserted item with ID: ${item.id}"
                            )
                        }
                    }
                } catch (ex: Exception) {
                    Log.e(
                        "FirestoreError",
                        "Error parsing document: ${ex.message}, ex"
                    )
                }
            }
        }
    }
} else {
    _error.postValue("User not authenticated")
}
}*/