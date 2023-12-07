package com.example.yandex1

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yandex1.models.TodoItem
import com.example.yandex1.ui.TodoListFragmentDirections
import com.example.yandex1.viewmodels.TodoViewModel

class TodoAdapter(private val viewModel: TodoViewModel) :
    ListAdapter<TodoItem, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivStatus: ImageView = itemView.findViewById(R.id.iv_status)
        val tvTodoText: TextView = itemView.findViewById(R.id.tv_todo_text)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun updateTextViewAppearance(isDone: Boolean, tv: TextView) {
            if (isDone) {
                tv.paintFlags = tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tv.setTextColor(ContextCompat.getColor(tv.context, R.color.colorSecondaryDark))
            } else {
                tv.paintFlags = tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tv.setTextColor(ContextCompat.getColor(tv.context, R.color.black))
            }
        }

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    viewModel.onTodoItemClicked(item.id)
                }
            }
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return TodoViewHolder(view).apply {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    val updatedItem = item.copy(isDone = isChecked)
                    viewModel.updateTodoItem(updatedItem)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val item = getItem(position)
        holder.apply {
            tvTodoText.text = item.text
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = item.isDone
            updateTextViewAppearance(item.isDone, tvTodoText)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                updateTextViewAppearance(isChecked, tvTodoText)
                // При изменении состояния чекбокса, меняем дроубл
                val updatedItem = item.copy(isDone = isChecked)  //Update item status
                viewModel.updateTodoItem(updatedItem)
            }
        }
    }
}


