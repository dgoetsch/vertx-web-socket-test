package dev.yn.playground.web.socket.server.vertx

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.http.ServerWebSocket
import java.util.*

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
            webSocket.pongHandler {
                println("[$context] Received Pong")
                resetTimer {
                    cancelPeriodicPing()
                    webSocket.close()
                }
            }
            resetPeriodic {
                println("[$context] Send Ping")
                webSocket.writePing(Buffer.buffer("ping"))
            }
        }.listen(port)
    }

    var timerId: Long? = null
    var periodicId: Long? = null
    private fun resetTimer(close: () -> Unit) = synchronized(this) {
        timerId?.let { vertx.cancelTimer(it) }
        timerId = vertx.setTimer(2000L, { close() } )
    }

    private fun resetPeriodic(sendPing: () -> Unit) = synchronized(this) {
        periodicId?.let { vertx.cancelTimer(it) }
        periodicId = vertx.setPeriodic(1000L, { sendPing() })
    }
    private fun cancelPeriodicPing() = synchronized(this) {
        periodicId?.let { vertx.cancelTimer(it) }
    }

    fun respond(context: UUID, message: String, webSocket: ServerWebSocket) {
        println("[SERVER] writing message to client")
        webSocket.writeTextMessage("response: $context")
    }
}

