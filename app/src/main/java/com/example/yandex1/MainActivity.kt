package com.example.yandex1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.yandex1.viewmodels.TodoViewModel
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: TodoViewModel.Factory

    companion object {
        const val RC_SIGN_IN = 812
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получаем ссылку на Application
        val application = getApplication() as TodoApplication
        application.todoComponent.inject(this)

        // Инициализируем viewModelFactory
        val todoItemsRepository = application.todoComponent.getTodoItemsRepository()

        // Инжектируем viewModelFactory. Закомментировал используем фабрику через Dagger2
        //viewModelFactory = TodoViewModel.Factory(todoItemsRepository)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
    }
}

