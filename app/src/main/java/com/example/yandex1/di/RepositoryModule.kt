package com.example.yandex1.di

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import com.example.yandex1.models.TodoItemsRepository


@Module
class RepositoryModule(private val context: Context) {

    @Singleton
    @Provides
    fun provideTodoItemsRepository(): TodoItemsRepository = TodoItemsRepository(context)
}
