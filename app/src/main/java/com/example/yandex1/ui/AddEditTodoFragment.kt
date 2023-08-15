package com.example.yandex1.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.yandex1.MainActivity
import com.example.yandex1.R
import com.example.yandex1.databinding.FragmentAddEditTodoBinding
import com.example.yandex1.models.Importance
import com.example.yandex1.models.TodoItem
import com.example.yandex1.viewmodels.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddEditTodoFragment : Fragment() {

    private var _binding: FragmentAddEditTodoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TodoViewModel by viewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }
    private val args: AddEditTodoFragmentArgs by navArgs()
    private var currentItem: TodoItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddEditTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        val editTextTodo = binding.etDescription

        val spinnerImportance = binding.spinnerImportance

        // Handle passed arguments (todoId)
        currentItem = args.todoId?.let {
            viewModel.getTodoItem(it)
        }
        binding.etDescription.setText(currentItem?.text)

        //Устанавливаем начальное значение для spinnerImportance на основе текущего элемента
        currentItem?.let {
            spinnerImportance.setSelection(
                when (it.importance) {
                    Importance.NORMAL -> 1
                    Importance.HIGH -> 2
                    else -> 0
                }
            )
        }

        // Set OnClickListener to Save, Delete, Close buttons
        binding.btnSave.setOnClickListener {
            val text = editTextTodo.text.toString()
            if (text.isNotBlank()) {
                val importance = when (spinnerImportance.selectedItem.toString()) {
                    "NORMAL" -> Importance.NORMAL
                    "HIGH" -> Importance.HIGH
                    else -> Importance.LOW
                }

                val todoItem = currentItem?.copy(
                    text = text,
                    importance = importance,
                    deadline = if (binding.switchDeadline.isChecked) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(binding.deadlineText.text.toString())
                else null) ?: TodoItem(
                    id = UUID.randomUUID().toString(),
                    text = text,
                    importance = importance,
                    isDone = false,
                    creationDate = Date()
                )

                if (currentItem != null) {
                    viewModel.updateTodoItem(todoItem) // Assuming you have this method in your ViewModel
                } else {
                    viewModel.addTodoItem(todoItem)
                }
                val action = AddEditTodoFragmentDirections.actionAddEditTodoFragmentToTodoListFragment()
                findNavController().navigate(action)
            }
        }

        binding.btnDelete.setOnClickListener {
            currentItem?.let {
                viewModel.deleteTodoItem(it)
                val action = AddEditTodoFragmentDirections.actionAddEditTodoFragmentToTodoListFragment()
                findNavController().navigate(action)

            }

        }

        binding.btnClose.setOnClickListener {
            findNavController().navigate(R.id.action_addEditTodoFragment_to_todoListFragment)
        }


        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_addEditTodoFragment_to_todoListFragment)
        }

        binding.switchDeadline.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val calendar = Calendar.getInstance()
                val datePicker = CustomDatePickerFragment()
                datePicker.listener = object : CustomDatePickerFragment.OnDateSetListener {
                    override fun onDateSet(year: Int, month: Int, day: Int) {
                        calendar.set(year, month, day)
                        val deadline = calendar.time
                        binding.deadlineText.text =
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(deadline)
                    }

                    override fun onCancel() {
                        binding.switchDeadline.isChecked = false
                    }
                }
                datePicker.show(requireFragmentManager(), "datePicker")
            } else {
                binding.deadlineText.text = ""
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
