package com.rgarage.domain

data class ListName(val name: String)

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)

enum class ToDoStatus { Todo, InProgress, Done, Blocked }
data class ToDoItem(
    val description: String
)