package com.example.yandex1.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.example.yandex1.R
import java.util.Calendar

class CustomDatePickerFragment : DialogFragment() {

    interface OnDateSetListener {
        fun onDateSet(year: Int, month: Int, day: Int)
        fun onCancel()
    }

    var listener: OnDateSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val view = DatePicker(context)
        view.updateDate(year, month, day)
        view.calendarViewShown = false

        return AlertDialog.Builder(context, R.style.AlertDialogTheme)
            .setView(view)
            .setPositiveButton("Готово") { _, _ ->
                val year = view.year
                val month = view.month
                val day = view.dayOfMonth
                listener?.onDateSet(year, month, day)
            }
            .setNegativeButton("Отмена") { _, _ ->
                listener?.onCancel()
            }
            .create()
    }
}
