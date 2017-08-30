package dev.yn.playground.web.socket.client.vertx

import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.RequestOptions
import io.vertx.core.http.WebSocket

class WebSocketClient(val port: Int, val name: String): AbstractVerticle() {

    val host = "localhost"

    val requestOptions = RequestOptions()
            .setHost(host)
            .setPort(port)
            .setURI("/websocket/echo")
            .setSsl(false)

    val httpClientOptions = HttpClientOptions().setKeepAlive(true).setIdleTimeout(3)
    override fun start() {
        vertx.createHttpClient(httpClientOptions).websocket(requestOptions, { webSocket: WebSocket ->
            webSocket.textMessageHandler { textMessage: String ->
                println("[$name] received: $textMessage")
            }

            println("[$name] writing to server")
            webSocket.writeTextMessage("Message to start!")

            webSocket.frameHandler {
                println("[$name] received frame: $it")
            }
        })

    }

}