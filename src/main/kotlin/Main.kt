package com.rgarage

import org.http4k.client.ApacheClient
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    println("Hello World!")

    val htmlPage = """ <html>
    <body>
        <h1 style="text-align:center; font-size:3em;" >
Hello Functional World!
        </h1>
    </body>
</html>"""

    //demo wil show html page
    //val app = {request: Request -> Response(Status.OK).body(htmlPage) }

    /* calling /todo/8/13 will show html on screen */
    val app :
        HttpHandler =  routes("/todo/{user}/{list}" bind Method.GET to  :: showList)

    val jettyServer = app.asServer(Jetty(9000)).start()


}

fun showList(req: Request) : Response {
    val user: String ? = req.path("user")
    val list: String? = req.path("list")

    val htmlPage = """
<html>
    <body>
        <h1>Zettai</h1>
        <p>Here is the list <b>$list</b> of user <b>$user</b></p>
    </body>
❹ </html>"""
    return Response(OK).body(htmlPage);
}