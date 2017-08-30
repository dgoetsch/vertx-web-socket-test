## Vertx Websocket Test

You can run the web socket server and client in separate jvms, or in the same jvm.

same jvm: `$ gradle web-socket-unified:run`

different jvm requires two terminal windows or the use of system utilies:

```
$ gradle web-socket-server:run

$ gradle web-socket-client:run
```

You can select the tyrus or vertx client/server by modifying application arguments in the relevant build.gradle file.

If web-socket-unified gets no argument, it runs a sample vertx client with a timeout