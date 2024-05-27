package com.rgarage

import com.rgarage.domain.ListName
import com.rgarage.domain.ToDoItem
import com.rgarage.domain.ToDoList
import com.rgarage.domain.User
import org.http4k.client.ApacheClient
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    val items = listOf("write chapter", "insert code", "draw diagrams")
    val toDoList = ToDoList(ListName("book"), items.map(::ToDoItem))
    val lists = mapOf(User("rickson") to listOf(toDoList) )
    val app: HttpHandler = Zettai(lists)
    app.asServer(Jetty(8080)).start() //starting the server
    println("Server started at http://localhost:8080/todo/uberto/book")


}
