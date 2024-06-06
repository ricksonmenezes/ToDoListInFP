package tooling

import com.rgarage.domain.ListName
import com.rgarage.domain.ToDoItem
import com.rgarage.domain.ToDoList
import com.rgarage.domain.User
import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainDrivenTest


interface ZettaiActions : DdtActions<DdtProtocol> {
    fun getToDoList(user: User, listName: ListName): ToDoList?
    fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>)
}


typealias ZettaiDDT = DomainDrivenTest<ZettaiActions>

fun allActions() = setOf(
    DomainOnlyActions(),
    HttpActions()
)