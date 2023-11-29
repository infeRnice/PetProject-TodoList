package com.example.yandex1.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yandex1.MainActivity
import com.example.yandex1.R
import com.example.yandex1.TodoAdapter
import com.example.yandex1.viewmodels.TodoViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


class TodoListFragment : Fragment() {

    private lateinit var adapter: TodoAdapter
    private val viewModel: TodoViewModel by viewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }
    private val signInActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    viewModel.syncDataWithFirestore()
                } catch (e: ApiException) {
                    viewModel.postError()
                }
            } else {
                viewModel.postError()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Устанавливаем адаптер для RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = TodoAdapter(viewModel)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        //Check if the user is authorized
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            // User is not authenticated, show UI elements for sign-in
            val signInButton = view.findViewById<Button>(R.id.signInButton)
            signInButton.visibility = View.VISIBLE

            signInButton.setOnClickListener {
                // Start Google Sign-In process
                val googleSignInIntent = viewModel.getGoogleSignInIntent()
                signInActivityResultLauncher.launch(googleSignInIntent)
            }
        }

        // Observe ViewModel's todoItems
        viewModel.todoItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        // Set OnClickListener to FloatingActionButton
        val fab = view.findViewById<FloatingActionButton>(R.id.addTodoButton)
        fab.setOnClickListener {
            val action =
                TodoListFragmentDirections.actionTodoListFragmentToAddEditTodoFragment(null)
            findNavController().navigate(action)
        }

        viewModel.selectedTodoId.observe(viewLifecycleOwner) { id ->
            id?.let {
                val action =
                    TodoListFragmentDirections.actionTodoListFragmentToAddEditTodoFragment(id)
                findNavController().navigate(action)
            }
        }

        viewModel.showSnackbarEvent.observe(viewLifecycleOwner) { message ->
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        }

        viewModel.error.observe(viewLifecycleOwner, { errorMessage ->
            viewModel.postError()
        })
    }
}
