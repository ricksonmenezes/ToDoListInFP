
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
import org.opentest4j.AssertionFailedError
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo


class SeeATodoListAT {

    @Test
    fun `List owners can see their lists`() {

        val listName = "shopping"
        val foodToBuy = listOf("carrots", "apples", "milk")


        val frank = ToDoListOwner("frank")

        /*when we start the app, we are sending the list into the  app start class Zettai*/
        startTheApplication(frank.name, listName, foodToBuy)
        val list = frank.getToDoList(frank.name, listName)
        //val list = getToDoList(userName, listName)

        expectThat(list.listName.name).isEqualTo(listName)
        expectThat(list.items.map { it.description }).isEqualTo(foodToBuy)
    }

    @Test
    fun `Only owners can see their lists`() {
        val listName = "shopping"
        val foodToBuy = listOf("carrots", "apples", "milk")
        val frank = ToDoListOwner("frank")
        startTheApplication(frank.name,listName, foodToBuy)
        frank.canSeeThelist(listName, foodToBuy)
    }





    interface ScenarioActor{ val name: String
    }

    class ToDoListOwner(override val name: String): ScenarioActor {

        /*@Test
        fun `Only owners can see their lists`() {
            val listName = "shopping"
            val foodToBuy = listOf("carrots", "apples", "milk")
            val frank = ToDoListOwner("frank")
            start(frank.name, createList(listName, foodToBuy))

            }*/
        fun canSeeThelist(listName: String, items: List<String>) {

            val expectedList = createList(listName, items);
            val list = getToDoList(name, listName )
            expectThat(list).isEqualTo(expectedList)

        }

        private fun createList(listname: String, items: List<String>) =
            ToDoList(ListName(listname), items.map ( ::ToDoItem ))

         fun getToDoList(user: String, listName: String): ToDoList {
            val client = JettyClient()
            val response = client(Request(Method.GET, "http://localhost:8081/todo/$user/$listName"))

            return if (response.status == Status.OK)
                parseResponse(response.bodyString())
            else
                fail(response.toMessage())
        }

        private fun parseResponse(html: String): ToDoList {
            val nameRegex = "<h2>.*<".toRegex()
            val listName = ListName(extractListName(nameRegex, html))
            val itemsRegex = "<td>.*?<".toRegex()
            val items = itemsRegex.findAll(html)
                .map { ToDoItem(extractItemDesc(it)) }.toList()
            return ToDoList(listName,items)
        }
        private fun extractListName(nameRegex: Regex, html: String): String = nameRegex.find(html)?.value
            ?.substringAfter("<h2>") ?.dropLast(1)
            .orEmpty()

        private fun extractItemDesc(matchResult: MatchResult): String = matchResult.value.substringAfter("<td>").dropLast(1)

    }

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
        /*when we start the app, we are sending the list into the  app start class Zettai
        * But Zettai has no variable to store the lists. It's  probably implicitly stored. */
        val server = Zettai(lists).asServer(Jetty(8081)) //different from main
        server.start()
    }

}