package com.example.yandex1.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.snackbar.Snackbar


class TodoListFragment : Fragment() {

    private lateinit var adapter: TodoAdapter
    private val viewModel: TodoViewModel by viewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = TodoAdapter(viewModel)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

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
    }
}
