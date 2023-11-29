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
import com.example.yandex1.database.TodoDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.withContext

class TodoItemsRepository(private val context: Context) {

    private val _error = MutableLiveData<String>()
    //val error: LiveData<String> get() = _error
    val error: LiveData<String> = _error
    // Использование сервиса подключения
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    // Инициализация обратного вызова сети
    private val networkCallback = NetworkCallbackImpl()
     //Firestore
    private val db = FirebaseFirestore.getInstance()
    private val todoCollection = db.collection("todos")
    private val auth = FirebaseAuth.getInstance()
    //Room database
    private val todoDao = TodoDatabase.getDatabase(context).todoDao()

    init {
        // Для мониторинга сети
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback
        )
    }

    //Синхронизация с Firestore
    suspend fun syncDataWithFirestore() = withContext(Dispatchers.IO) {
        //CoroutineScope(Dispatchers.IO).launch {
            try {
                fetchDataFromFirestore()
            } catch (e: Exception) {
                _error.postValue("Failed to synchronize with Firestore: ${e.message}")
            }

    }

    fun getGoogleSignInIntent(): Intent {
        //Start Google sign-in process
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1078527184911-ne3l9r892vokqjchttememr43rjcd6ht.apps.googleusercontent.com")
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
                } catch (e: Exception) {
                    _error.postValue("Failed to push item to Firestore: ${e.message}")
                }
            }
        }
    }

    private suspend fun fetchDataFromFirestore() = withContext(Dispatchers.IO) {
        auth.currentUser?.let { user ->
            todoCollection.whereEqualTo("userId", user.uid).get().addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { document ->
                    document.toObject(TodoItem::class.java)?.let { todoDao.insertTodoItem(it) }
                }
            }.addOnFailureListener { e ->
                _error.postValue("Error fetching data: ${e.localizedMessage}")
            }
        } ?: _error.postValue("User not authenticated")
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

    fun getTodoItems(): LiveData<List<TodoItem>> {
        Log.d("TodoListFragment", "getTodoItems() called")
        return todoDao.getAllTodoItem()
    }

    suspend fun addTodoItem(item: TodoItem) = withContext(Dispatchers.IO) {
        Log.d("FirestoreAdd", "Adding todo item: ${item.id}")
        todoCollection.document(item.id).set(item).await()
        todoDao.insertTodoItem(item)
    }

    suspend fun deleteTodoItem(item: TodoItem) = withContext(Dispatchers.IO) {
        todoCollection.document(item.id).delete().await()
        todoDao.deleteTodoItem(item.id)
    }

    suspend fun updateTodoItem(updatedItem: TodoItem) = withContext(Dispatchers.IO) {
        todoCollection.document(updatedItem.id).set(updatedItem).await()
        todoDao.updateTodoItem(updatedItem)
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
            //Consider triggering data sync here
            /*object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    fetchDataFromFirestore()
                }

                override fun onLost(network: Network) {
                    _error.postValue("No internet connection")
                }
            }*/
        }

        override fun onLost(network: Network) {
            _error.postValue("No internet connection")
        }
    }
}