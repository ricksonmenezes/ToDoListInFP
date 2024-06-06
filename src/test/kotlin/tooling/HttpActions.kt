package tooling

import com.rgarage.Zettai
import com.rgarage.domain.*
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.Ready
import org.http4k.client.JettyClient
import org.http4k.core.Response
import org.http4k.core.Request
import org.http4k.core.Method
import org.http4k.filter.CachingFilters
import org.http4k.server.Jetty
import org.http4k.server.asServer


data class HttpActions(val env: String = "local"): ZettaiActions {

    //private val fetcher = ToDoListFetcherFromMap(mutableMapOf())

    val frank = ToDoListOwner("Frank")
    //val frank = ToDoListOwner(listName)
    val shoppingItems = listOf("carrots", "apples", "milk")
    //val toDoList = ToDoList(ListName(frank.name), items.map ( ::ToDoItem ))
    val toDoList = ToDoList(ListName(frank.name), shoppingItems.map ( ::ToDoItem ))

    val hub = ToDoListHub(mapOf(User(frank.name) to listOf(toDoList)))

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {


    }
    override fun getToDoList(user: User, listName: ListName): ToDoList = TODO("not implemented yet")

    override val protocol: DdtProtocol = Http(env)

    val zettaiPort = 8000 //different from the one in main
     val server = Zettai(hub).asServer(Jetty(zettaiPort))

    val client = JettyClient()
    override fun prepare(): DomainSetUp {
        return Ready }
    override fun tearDown(): HttpActions = also { server.stop() }
    private fun callZettai(method: Method, path: String): Response =
        client(log( Request(
        method, "http://localhost:$zettaiPort/$path")))

    fun <T> log(something: T): T {
        println("--- $something")
        return something
    }
}