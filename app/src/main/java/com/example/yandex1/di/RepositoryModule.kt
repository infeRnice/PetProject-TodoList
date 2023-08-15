    package com.example.yandex1.di

    import dagger.Module
    import dagger.Provides
    import javax.inject.Singleton
    import com.example.yandex1.models.TodoItemsRepository

    @Module
    class RepositoryModule {

        @Singleton
        @Provides
        fun provideTodoItemsRepository(): TodoItemsRepository = TodoItemsRepository()
    }
