                                                # nautchkafe-rabbitmq 

**Lightweight, flexible, and functional message-passing pubsub system built with RabbitMQ.  
This project allows easy publishing and subscribing of messages using a publish-subscribe architecture.**

## Features

- Publisher and Subscriber Interfaces: Simplifies sending and receiving messages through RabbitMQ using the publish-subscribe pattern.
- Lightweight and Extensible: Easily configurable RabbitMQ setup with the ability to extend or customize the transport layer to fit various message delivery needs.
- Pure Functions: The code is designed with pure functions, eliminating side effects and making it easier to reason about and 
- No simulation of errors or results; simply publish and subscribe to topics in a straightforward manner.
- Clean, minimalistic design with focus on decoupling the message transport logic from application logic.
- Supports multiple topics (queues) for publishing and subscribing messages in a highly decoupled manner.


## Components
- RabbitPublisher<TOPIC>: An interface to send messages to a specific RabbitMQ topic.
- RabbitSubscriber<TOPIC>: An interface to handle incoming messages from a RabbitMQ topic.
- RabbitTransport<TOPIC>: Implementation that connects to RabbitMQ, publishes messages, and subscribes to topics.
- RabbitClientCredintials: A factory class that handles creating and managing connections to RabbitMQ using the provided configuration.

### Clone the Repository

To get started, clone the repository and build the project:

```bash
git clone https://github.com/noyzys/nautchkafe-rabbit-bridge.git
cd nautchkafe-rabbit-bridge
```

## Example use case

```java

public static void main(String[] args) {
    
    // Base scope configuration, register TOPIC
    RabbitClientConnector clientConnector = new RabbitClientConnector();
    RabbitClientResource<RabbitTransport<RabbitUser>> resource = RabbitTransport.createResource(clientConnector);

    // Sync
    resource.use(transport -> {
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.publish("server", "sync message"))
            .peek(result -> result.subscribe("server", message -> {

            System.out.println("> Received message: " + message);
            return Either.right(null);

        })).peek(result -> result.close());

        // Async
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.publishAsync("server_async", "async message"))
            .peek(result -> result.subscribeAsync("server_async", message -> {

            System.out.println("> Received async message: " + message);
            return Future.successful(null);

        })).peek(result -> result.closeAsync().onFailure(error -> System.err.println("Failed to close: " + error.getMessage())));
    });
}

// BY DEFAULT JACKSON DATABIND OBJECT SERDES.
```

## Expected Output sout (Console)
```
publish, subscribe topic -> "server", "server_async"
> Received message: sync message
> Received async message: async message
```

**If you are interested in exploring functional programming and its applications within this project visit the repository at [vavr-in-action](https://github.com/noyzys/bukkit-vavr-in-action), [fp-practice](https://github.com/noyzys/fp-practice).**