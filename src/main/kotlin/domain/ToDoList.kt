package com.rgarage.domain

data class ListName(val name: String)

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)

data class HtmlPage(val raw: String)
enum class ToDoStatus { Todo, InProgress, Done, Blocked }
data class ToDoItem(
    val description: String
)

/*
data class  ZettaiHub(val user: User, val listName: ListName)*/
