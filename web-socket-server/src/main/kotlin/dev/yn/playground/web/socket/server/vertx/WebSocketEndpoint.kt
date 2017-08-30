package dev.yn.playground.web.socket.server.vertx

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.http.ServerWebSocket
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class WebSocketServer(val port: Int): AbstractVerticle() {

    val httpServerOptions = HttpServerOptions()
            .setIdleTimeout(5)
            .setTcpKeepAlive(true)
    override fun start(startFuture: Future<Void>?) {
        vertx.createHttpServer(httpServerOptions).websocketHandler { webSocket: ServerWebSocket ->
            val context = UUID.randomUUID()
            println("[SERVER][$context] received request" +
                    "\t\npath:\t${webSocket.path()}" +
                    "\t\nquery:\t${webSocket.query()}" +
                    "\t\nheaders:\t${webSocket.headers()}" +
                    "\t\nuri:\t${webSocket.uri()}")
            webSocket.textMessageHandler { textMessage ->
                respond(context, textMessage, webSocket)
            }
            webSocket.closeHandler {
                println("closing $context")
            }
        }.listen(port)
    }

    fun respond(context: UUID, message: String, webSocket: ServerWebSocket) {
        println("[SERVER] writing message to client")
        webSocket.writeTextMessage("response: $context")
    }
}

