package com.example.yandex1

import android.app.Application
import com.example.yandex1.di.DaggerTodoComponent
import com.example.yandex1.di.RepositoryModule

class TodoApplication : Application() {

    val todoComponent by lazy {
        DaggerTodoComponent.factory().create(RepositoryModule())
    }
}
