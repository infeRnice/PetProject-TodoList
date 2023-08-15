    package com.example.yandex1.di

    import com.example.yandex1.MainActivity
    import com.example.yandex1.viewmodels.TodoViewModel
    import com.example.yandex1.models.TodoItemsRepository
    import dagger.Component
    import javax.inject.Singleton

    @Singleton
    @Component(modules = [RepositoryModule::class])
    interface TodoComponent {

        fun inject(mainActivity: MainActivity)

        fun getTodoItemsRepository(): TodoItemsRepository

        @Component.Factory
        interface Factory {
            fun create(repositoryModule: RepositoryModule): TodoComponent
        }
    }
