package dev.yn.playground.web.socket.server

import io.vertx.core.Vertx

/**
 * Created by devyn on 8/28/17.
 */
fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    when(args.getOrNull(0)) {
        "tyrus" -> {
            dev.yn.playground.web.socket.server.tyrus.runServer()
            while(true) {
                println("Server is running..")
                Thread.sleep(10000L)
            }
        }
        "vertx" -> {
            val vertx = Vertx.vertx()
            vertx.deployVerticle(dev.yn.playground.web.socket.server.vertx.WebSocketServer(8025))
        }
        else -> {
            println("invalid configuration, exiting..")
        }
    }
}