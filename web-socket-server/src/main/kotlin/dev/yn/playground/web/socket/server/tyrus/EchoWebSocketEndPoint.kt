package dev.yn.playground.web.socket.server.tyrus

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.logging.LoggerFactory
import org.glassfish.tyrus.server.Server
import javax.websocket.*
import javax.websocket.server.ServerEndpoint
import java.nio.ByteBuffer
import java.util.HashMap

object Singleton {
    val vertx: Vertx by lazy { Vertx.vertx() }
}

object SessionEvents {
    fun closeRoutingKey(sessionId: String): String =  "close.$sessionId"
    fun pongRoutingKey(sessionId: String): String = "pong.$sessionId"
}

fun runServer() {
    val server = Server("localhost", 8025, "/websocket", HashMap(), EchoWebSocketEndPoint::class.java)
    try {
        server.start()
    } catch (e: Throwable) {
        println("could not start server because $e")
        e.printStackTrace()
    }
}

@ServerEndpoint(value = "/echo")
class EchoWebSocketEndPoint {
    val vertx: Vertx = Singleton.vertx

    val logger = LoggerFactory.getLogger(this.javaClass)

    @OnMessage
    fun textMessage(message: String, session: Session) {
        logger.info("session ${session.id} recevied text message: $message")
    }

    @OnMessage
    fun getMessage(message: PongMessage, session: Session) {
        logger.info("session ${session.id} received pong message")
        vertx.eventBus().send(SessionEvents.pongRoutingKey(session.id), Buffer.buffer("pong"))
    }

    @OnOpen
    fun onOpoen(session: Session) {
        logger.info("opening session ${session.id}")
        vertx.deployVerticle(SessionVerticle(session))
    }

    @OnClose
    fun onClose(session: Session) {
        logger.info("closing session ${session.id}")
        vertx.eventBus().send(SessionEvents.closeRoutingKey(session.id), Buffer.buffer("close"))
    }


}

class SessionVerticle(val session: Session): AbstractVerticle() {
    val logger = LoggerFactory.getLogger(this.javaClass)

    var closeTimerId: Long? = null
    override fun start() {
        vertx.eventBus().consumer<Buffer>(SessionEvents.closeRoutingKey(session.id), {
            vertx.undeploy(deploymentID())
        })

        vertx.eventBus().consumer<Buffer>(SessionEvents.pongRoutingKey(session.id), {
            logger.info("session ${session.id} restarting pong timer")
            restartCloseTime()
        })


        restartCloseTime()
        session.asyncRemote.sendPing(ByteBuffer.wrap("ping".toByteArray()))
        vertx.setPeriodic(1000, {
            logger.info("pinging ${session.id}")
            session.asyncRemote.sendPing(ByteBuffer.wrap(it.toString().toByteArray()))
        })
        session.asyncRemote.sendText("Welcome!")
    }

    override fun stop() {
        cancelCloseTimer()
        if(session.isOpen) {
            logger.info("closing session ${session.id}")
            try {
                session.close()
            } catch(t: Throwable) {

            }
        }

    }

    private fun cancelCloseTimer(): Unit = synchronized(this) {
        closeTimerId?.let {
            vertx.cancelTimer(it)
        }
    }

    private fun restartCloseTime(): Unit = synchronized(this) {
        cancelCloseTimer()
        closeTimerId = vertx.setTimer(2000L, {
            logger.info("session ${session.id} no pong received")
            vertx.undeploy(deploymentID())
        })
    }


}