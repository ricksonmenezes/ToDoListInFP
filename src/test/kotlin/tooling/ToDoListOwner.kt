package tooling

import com.rgarage.domain.ListName
import com.rgarage.domain.ToDoList
import com.rgarage.domain.User
import com.ubertob.pesticide.core.DdtActor
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isNotNull


data class ToDoListOwner(override val name: String): DdtActor<ZettaiActions>() {

    val user = User(name)
    fun `can see #listname with #itemnames`( listName: String,
                                             expectedItems: List<String>) =

        step(listName, expectedItems) {
            val list = getToDoList(user, ListName(listName))
            expectThat(list)
                .isNotNull()
                .itemNames
                .containsExactlyInAnyOrder(expectedItems)
        }
    private val Assertion.Builder<ToDoList>.itemNames
        get() = get { items.map { it.description } }



}