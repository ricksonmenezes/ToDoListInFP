
import com.rgarage.Zettai
import com.rgarage.domain.ListName
import com.rgarage.domain.ToDoItem
import com.rgarage.domain.ToDoList
import com.rgarage.domain.User
import org.http4k.client.JettyClient
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo


class SeeATodoListAT {

    val frank = ToDoListOwner("Frank")
    val shoppingItems = listOf("carrots", "apples", "milk")
    val frankList = frank.createList("shopping", shoppingItems)
    val bob = ToDoListOwner("Bob")
    val gardenItems = listOf("fix the fence", "mowing the lawn")
    val bobList =  bob.createList("gardening", gardenItems)
    val lists = mapOf(
        frank.asUser() to listOf(frankList),
        bob.asUser() to listOf(bobList)
    )

    fun ToDoListOwner.asUser() : User = User(name)

    @Test
    fun `List owners can see their lists`() {

        val app = startTheApplication(lists);
        app.runScenario {
            frank.canSeeTheList("shopping", shoppingItems, it)
            bob.canSeeTheList("gardening", gardenItems, it)
        }
    }


    @Test
    fun `Only owners can see their lists`() {
        val app = startTheApplication(lists);
        app.runScenario {
            frank.canNotSeeTheList("gardening", shoppingItems, it)
            bob.canNotSeeTheList("shopping", gardenItems, it)
        }
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
        fun canSeeTheList(listName: String, items: List<String>, app: ApplicationForAT) {

            val expectedList = createList(listName, items);
            val list = app.getToDoList(name, listName )
            expectThat(list).isEqualTo(expectedList)

        }

        //reason why list is not passsed for this test is because the API should fail merely with user and listname
        //otoh, with canSeeTheList, while the API should pass, the list returned should be the same as expected list
        fun canNotSeeTheList(listName: String, items: List<String>, app: ApplicationForAT) {

            expectThrows<AssertionFailedError> {
                app.getToDoList(name, listName)
            }

        }

        fun createList(listname: String, items: List<String>) =
            ToDoList(ListName(listname), items.map ( ::ToDoItem ))


    }

    class ApplicationForAT(val client: HttpHandler, val server: AutoCloseable) {

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

        fun runScenario(steps: (ApplicationForAT)->Unit) { server.use {
            steps(this) }
        }
    }

    }

    private fun startTheApplication(
        lists : Map<User, List<ToDoList>>
    ) : SeeATodoListAT.ApplicationForAT {

        val port = 8081

        /*when we start the app, we are sending the list into the  app start class Zettai
        * But Zettai has no variable to store the lists. It's  probably implicitly stored. */
        val server = Zettai(lists).asServer(Jetty(8081)) //different from main
        server.start()

        val client = ClientFilters .SetBaseUriFrom(Uri.of("http://localhost:$port/")) .then(JettyClient())
        return SeeATodoListAT.ApplicationForAT(client, server)

    }

}