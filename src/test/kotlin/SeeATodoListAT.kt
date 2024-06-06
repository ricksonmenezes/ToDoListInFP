
import com.rgarage.Zettai
import com.rgarage.domain.*
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

/*
* Let's break down the code snippet and address your questions:
step(this) in runScenario:
In the runScenario function, steps.onEach { step -> step(this) } iterates over each step in the steps array and calls it with this as the receiver.
step(this) invokes the step function with this as the receiver. This means that the function step is called as an extension function on the object represented by this.
this in runScenario:
In the context of runScenario, this represents the current instance of the Actions interface. Since runScenario is likely a member function of a class that implements Actions, this refers to the instance of that class.
When step(this) is called, it means that the step function is executed as an extension function on the current instance of the class that implements Actions.
Calling frank.canSeeTheList("shopping", shoppingItems):
When you pass frank.canSeeTheList("shopping", shoppingItems) as a step to runScenario, it is added to the steps array.
Internally, when steps.onEach { step -> step(this) } is executed, each step in the steps array is called with this as the receiver. This means that frank.canSeeTheList("shopping", shoppingItems) is called internally with this referring to the current instance of the class that implements Actions.
In summary, step(this) in runScenario calls each step function as an extension function on the current instance of the class that implements Actions. The this keyword represents the instance of the class, and when frank.canSeeTheList("shopping", shoppingItems) is passed as a step, it is internally called with this referring to that instance. This concise syntax allows for the execution of each step function within the context of the Actions interface implementation.

* * app should not be able to call frank.canSeeTheList but this is exactly what is happening. It is added as an extenstion to the Action interface
* So app, apart from calling the step lambda which is functions of Action i.e getToDoList(name,listname),  app is also able to call canSeetheList as if it were a function of the ApplicationForAT class that is represented by "this" inside the
* run scenario function. All this is done to remove the app object that is being passed into the ToDoListOwner class functions
* previous this function was called by doing the following   frank.canSeeTheList("shopping", shoppingItems, app)
*
*  fun canSeeTheList(listName: String, items: List<String>, app: ApplicationForAt)  {

            val expectedList = createList(listName, items);
            val list = app.getToDoList(name, listName)
            expectThat(list).isEqualTo(expectedList)

        }
* */

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
        app.runScenario (
            frank.canSeeTheList("shopping", shoppingItems),
            bob.canSeeTheList("gardening", gardenItems)
        )
    }


    @Test
    fun `Only owners can see their lists`() {
        val app = startTheApplication(lists);
        //we are passing this function "frank.cannotseethelist() along with the app object it)
        app.runScenario (
            frank.canNotSeeTheList("gardening"),
            bob.canNotSeeTheList("shopping")
        )
    }

    interface ScenarioActor{ val name: String
    }

    interface Actions{
        fun getToDoList(user: String, listName: String): ToDoList?
    }

    class ToDoListOwner(override val name: String): ScenarioActor {

        /*@Test
        fun `Only owners can see their lists`() {
            val listName = "shopping"
            val foodToBuy = listOf("carrots", "apples", "milk")
            val frank = ToDoListOwner("frank")
            start(frank.name, createList(listName, foodToBuy))

            }*/
        fun canSeeTheList(listName: String, items: List<String>) : Step = {

            val expectedList = createList(listName, items);
            val list = getToDoList(name, listName )
            expectThat(list).isEqualTo(expectedList)

        }

        //reason why list is not passsed for this test is because the API should fail merely with user and listname
        //otoh, with canSeeTheList, while the API should pass, the list returned should be the same as expected list
        fun canNotSeeTheList(listName: String) : Step = {

            expectThrows<AssertionFailedError> {
                getToDoList(name, listName)
            }

        }

        fun createList(listname: String, items: List<String>) =
            ToDoList(ListName(listname), items.map ( ::ToDoItem ))


    }

    class ApplicationForAT(val client: HttpHandler, val server: AutoCloseable) : Actions {

        override fun getToDoList(user: String, listName: String): ToDoList {
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

        fun runScenario(vararg steps: Step) {

            server.use {steps.onEach { step -> step(this) }  }

        }
    }


    private fun startTheApplication(
        lists : Map<User, List<ToDoList>>
    ) : SeeATodoListAT.ApplicationForAT {

        val port = 8081

        /*when we start the app, we are sending the list into the  app start class Zettai
        * But Zettai has no variable to store the lists. It's  probably implicitly stored. */
        val server = Zettai(ToDoListHub(lists)).asServer(Jetty(8081)) //different from main
        server.start()

        val client = ClientFilters .SetBaseUriFrom(Uri.of("http://localhost:$port/")) .then(JettyClient())
        return SeeATodoListAT.ApplicationForAT(client, server)

    }

}

typealias Step = SeeATodoListAT.Actions.() -> Unit