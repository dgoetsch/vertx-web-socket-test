package dev.yn.playground.web.socket.client

import io.vertx.core.Vertx

fun main(args: Array<String>) {
    if(args.size > 0) {
        when(args.get(0)) {
            "tyrus" -> {
                val session = dev.yn.playground.web.socket.client.tyrus.runClient()

                while(session?.isOpen()?:false) {
                    println("Client is running..")
                    Thread.sleep(10000L)
                }
                println("Client stopped")
            }
            "vertx" -> {
                val vertx = Vertx.vertx()
                vertx.deployVerticle(dev.yn.playground.web.socket.client.vertx.WebSocketClient(8025, "client-01"))
            }
            else -> println("invalid configuration, exiting")
        }
    }
}