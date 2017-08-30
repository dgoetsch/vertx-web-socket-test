package dev.yn.playground.web.socket

import dev.yn.playground.web.socket.server.tyrus.Singleton
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.*
import java.lang.Math.abs
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {

    val appArgs = args.map { it.split("=") }
            .filter { it.size == 2}
            .map { it.get(0) to it.get(1) }.toMap()
    if(appArgs.isEmpty()) {
        test()
    } else {
        when (appArgs.get("server")) {
            "tyrus" -> {
                dev.yn.playground.web.socket.server.tyrus.runServer()
            }
            "vertx" -> {
                Singleton.vertx.deployVerticle(dev.yn.playground.web.socket.server.vertx.WebSocketServer(8025))
            }
            else -> {
                println("invalid configuration, exiting..")
            }
        }
        Thread.sleep(1000)

        when (appArgs.get("client")) {
            "tyrus" -> {
                dev.yn.playground.web.socket.client.tyrus.runClient()

                while (true) {
                    println("Client is running..")
                    Thread.sleep(10000L)
                }
            }
            "vertx" -> {
                Singleton.vertx.deployVerticle(dev.yn.playground.web.socket.client.vertx.WebSocketClient(8025, "client-01"))
            }
            else -> println("invalid configuration, exiting")
        }
    }
}

fun test() {
    val vertx = Vertx.vertx()

    val clientCountDownLatch = CountDownLatch(1)
    val serverCountDownLatch = CountDownLatch(1)
    val serverFrames = ConcurrentLinkedQueue<String>()
    val clientFrames = ConcurrentLinkedQueue<String>()
    val serverClosed = AtomicBoolean(false)
    val clientClosed = AtomicBoolean(false)
    val idleTimeoutSeconds = 3
    val port = 8025
    val serverVerticle = TestWebSocketServer(port, idleTimeoutSeconds, serverCountDownLatch, serverFrames, serverClosed)
    val clientVerticle = TestWebSocketClient(port, idleTimeoutSeconds, clientCountDownLatch, clientFrames, clientClosed)
    vertx.deployVerticle(serverVerticle)
    vertx.deployVerticle(clientVerticle)
    while(serverClosed.get()) {
        Thread.sleep(100)
        println("still closed")
    }
    val start = System.currentTimeMillis()
    println("open")
    serverCountDownLatch.await()
    clientCountDownLatch.await()
    println("closed")
    val fin = System.currentTimeMillis()
    assert(abs(fin - start) < idleTimeoutSeconds + 500)
    assert(abs(fin- start) > idleTimeoutSeconds - 500)
    vertx.close()
}

class TestWebSocketServer(val port: Int, val idleTimeoutSeconds: Int, val countDownLatch: CountDownLatch, val frames: ConcurrentLinkedQueue<String>, val isClosed: AtomicBoolean): AbstractVerticle() {

    val httpServerOptions = HttpServerOptions()
            .setIdleTimeout(idleTimeoutSeconds)
            .setTcpKeepAlive(true)

    override fun start(startFuture: Future<Void>) {
        vertx.createHttpServer(httpServerOptions).websocketHandler { webSocket: ServerWebSocket ->
            isClosed.set(true)
            webSocket.frameHandler {
                frames.add(it.toString())
            }
            webSocket.closeHandler {
                isClosed.set(false)
            }
        }.listen(port)
        startFuture.complete()
    }
}

class TestWebSocketClient(val port: Int, val idleTimeoutSeconds: Int, val countDownLatch: CountDownLatch, val frames: ConcurrentLinkedQueue<String>, val isClosed: AtomicBoolean): AbstractVerticle() {

    val host = "localhost"

    val requestOptions = RequestOptions()
            .setHost(host)
            .setPort(port)
            .setURI("/websocket/echo")
            .setSsl(false)

    val httpClientOptions = HttpClientOptions()
            .setKeepAlive(true)
            .setTcpKeepAlive(true)
            .setIdleTimeout(idleTimeoutSeconds)

    override fun start(startFuture: Future<Void>) {
        vertx.createHttpClient(httpClientOptions).websocket(requestOptions, { webSocket: WebSocket ->
            isClosed.set(false)
            webSocket.frameHandler {
                frames.add(it.toString())
            }
            webSocket.closeHandler {
                isClosed.set(true)
            }
            startFuture.complete()
        })

    }

}