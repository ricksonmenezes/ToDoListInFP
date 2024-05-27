
import com.rgarage.Zettai
import com.rgarage.domain.ListName
import com.rgarage.domain.ToDoItem
import com.rgarage.domain.ToDoList
import com.rgarage.domain.User
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.isEqualTo


class SeeATodoListAT {

    @Test
    fun `List owners can see their lists`() {

        val listName = "shopping"
        val foodToBuy = listOf("carrots", "apples", "milk")
        val userName = "frank"

        startTheApplication(userName, listName, foodToBuy)

        val list = getToDoList(userName, listName)

        expectThat(list.listName.name).isEqualTo(listName)
        expectThat(list.items.map { it.description }).isEqualTo(foodToBuy)
    }

    private fun getToDoList(user: String, listName: String): ToDoList {
        val client = JettyClient()
        val response = client(Request(Method.GET, "http://localhost:8081/todo/$user/$listName"))

        return if (response.status == Status.OK)
            parseResponse(response.bodyString())
        else
            fail(response.toMessage())
    }



   /* private fun parseResponse(html: String): ToDoList {
        val nameRegex = "<h2>.*<".toRegex()
        val listName = ListName(extractListName(nameRegex, html))
        val itemsRegex = "<td>.*?<".toRegex()
        val items = itemsRegex.findAll(html)
            .map { ToDoItem(extractItemDesc(it)) }.toList()
        return ToDoList(listName, items)
    }*/

    private fun parseResponse(html: String): ToDoList = TODO("parse the response")


    private fun startTheApplication(
        user: String,
        listName: String,
        items: List<String>
    ) {
        val toDoList = ToDoList(
            ListName(listName),
            items.map(::ToDoItem)
        )
        val lists = mapOf(User(user) to listOf(toDoList))
        val server = Zettai().asServer(Jetty(8081)) //different from main
        server.start()
    }

}