package dev.yn.playground.web.socket.client.tyrus

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import org.glassfish.tyrus.client.ClientManager
import java.net.URI
import javax.websocket.*

object Singleton {
    val LOG = LoggerFactory.getLogger(this.javaClass)
}
class WebSocketClient : Endpoint() {

    override fun onOpen(session: Session, config: EndpointConfig) {
        session.addMessageHandler(object : MessageHandler.Whole<PongMessage> {
            override fun onMessage(message: PongMessage) {
                println("received ping ${String(message.applicationData.array())}")
            }
        })
        session.addMessageHandler(object : MessageHandler.Whole<String> {
            override fun onMessage(message: String) {
                println(message)
            }
        })
        println(session.messageHandlers)
    }
}


fun runClient(): Session? {
    val client = ClientManager.createClient()
    val cec = ClientEndpointConfig.Builder.create().build()
    return try {
        client.connectToServer(WebSocketClient(), cec, URI("ws://localhost:8025/websocket/echo"))
//        session.addMessageHandler(object : MessageHandler.Whole<String> {
//            override fun onMessage(message: String) {
//                println(message)
//            }
//        })
//        session.addMessageHandler(object : MessageHandler.Whole<PongMessage> {
//            override fun onMessage(message: PongMessage) {
//                println("received ping ${String(message.applicationData.array())}")
//            }
//        })

    } catch (e: Throwable) {
        println("could not start client because $e")
        e.printStackTrace()
        null
    }
}