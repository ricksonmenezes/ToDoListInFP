package com.rgarage

import com.rgarage.domain.*
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

class Zettai(val lists: Map<User, List<ToDoList>>): HttpHandler {

    val routes = routes(
        "/todo/{user}/{list}" bind Method.GET to ::showList
    )
    override fun invoke(req: Request): Response = routes(req)

    private fun showList(request: Request): Response = request
        .let(::extractListData)
        .let(::fetchListContent)
        .let(::renderHtml)
        .let(::createResponse)


    private  fun extractListData(request: Request): Pair<User, ListName> {
        val user = request.path("user").orEmpty()
        val list = request.path("list").orEmpty()
        return User(user) to ListName(list)
    }


    fun fetchListContent(listId: Pair<User, ListName>): ToDoList =
        lists[listId.first]
        ?.firstOrNull { it.listName == listId.second }
        ?: error("List unknown")


    fun renderHtml(todoList: ToDoList): HtmlPage =
    HtmlPage("""
                <html>
                    <body>
                        <h1>Zettai</h1>
                        <h2>${todoList.listName.name}</h2>
                        <table>
                            <tbody>${renderItems(todoList.items)}</tbody>
                        </table>
                    </body>
                </html>
                """.trimIndent() )

    fun renderItems(items: List<ToDoItem>) = items.map {
        """<tr><td>${it.description}</td></tr>""".trimIndent() }.joinToString("")
    fun createResponse(html: HtmlPage): Response = Response(Status.OK).body(html.raw)
}
