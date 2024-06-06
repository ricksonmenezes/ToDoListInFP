package tooling

import com.rgarage.domain.ListName
import com.rgarage.domain.ToDoList
import com.rgarage.domain.ToDoListHub
import com.rgarage.domain.User
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready

class DomainOnlyActions(): ZettaiActions {

    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready

    private val lists: Map<User, List<ToDoList>> = emptyMap()

    private val hub = ToDoListHub(lists)

    override fun getToDoList(user: User, listName: ListName): ToDoList? = hub.getList(user, listName)

     /*fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val newList = ToDoList.build(listName, items)
        fetcher.assignListToUser(user, newList)
    }*/
}