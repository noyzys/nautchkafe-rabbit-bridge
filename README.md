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

## NOTE without Lockable
- When a message is delivered to a consumer, it becomes unavailable to other consumers until it is either acknowledged or rejected.
This mechanism eliminates the risk of racecondition, deadlock, ... between consumers attempting to access the same message.
- Message Acknowledgments (ACK)
RabbitMQ employs an acknowledgment system to manage the receipt and processing of messages:

Consumer acknowledgment (ack): The consumer informs RabbitMQ that the message has been successfully processed.
If the message is not acknowledged within a specified time frame (or if the consumer crashes), RabbitMQ requeues the message, making it available for redelivery.
- Prefetching and Load Management
RabbitMQ allows the configuration of a prefetch count parameter, which controls the number of messages delivered to a consumer at a time:

This limits the number of messages processed concurrently by a single consumer.
It prevents scenarios where a consumer becomes overwhelmed with an excessive number of tasks.

## Components
- RabbitPublisher<TOPIC>: An interface to send messages to a specific RabbitMQ topic.
- RabbitSubscriber<TOPIC>: An interface to handle incoming messages from a RabbitMQ topic.
- RabbitTransport<TOPIC>: Implementation that connects to RabbitMQ, publishes messages, and subscribes to topics.
- RabbitLockMapper: Implementation for lock acquire system in java.concurrent.
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

    // Example RabbitUser message
    RabbitUser userMessage = new RabbitUser("Frankie", 40);

    // Sync
    resource.use(transport -> {
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.publish("server", userMessage))
            .peek(result -> result.subscribe("server", message -> {

            System.out.println("> Received message: " + message);
            return Either.right(null);

        })).peek(result -> result.close());

        // Async
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.publishAsync("server_async", userMessage))
            .peek(result -> result.subscribeAsync("server_async", message -> {

            System.out.println("> Received async message: " + message);
            return Future.successful(null);

        })).peek(result -> result.closeAsync().onFailure(error -> System.err.println("Failed to close: " + error.getMessage())));
    });
}

// BY DEFAULT JACKSON DATABIND OBJECT SERDES.
```

## Multiple operations
```java
public static void main(String[] args) {

    RabbitClientConnector clientConnector = new RabbitClientConnector();
    RabbitClientResource<RabbitTransport<RabbitUser>> resource = RabbitTransport.createResource(clientConnector);

    resource.use(transport -> {
        // List of topics to subscribe to and publish to
        List<String> topics = List.of("server-1", "server-2", "server-3");

        // Example RabbitUser message
        RabbitUser userMessage = new RabbitUser("Frankie", 40);

        // Publish a message to multiple topics synchronously
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.publishMultiple(topics, userMessage))
            .peek(result -> System.out.println("> Published message to multiple topics"));

        // Subscribe to multiple topics asynchronously
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.subscribeToMultipleTopics(topics, message -> {
            
            System.out.println("> Received message from topic: " + message);
            return Future.successful(null); // Handle asynchronously
        })).peek(result -> System.out.println("> Subscribed to multiple topics"));

        // Publish a message asynchronously to multiple topics
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.publishMultipleAsync(topics, userMessage))
            .peek(result -> System.out.println("> Published async message to multiple topics"));

        // Subscribe to multiple topics asynchronously with async handling
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.subscribeToMultipleTopics(topics, message -> {

                System.out.println("Received async message from topic: " + message);
                return Future.successful(null); // Handle asynchronously
        })).peek(result -> System.out.println("> Subscribed asynchronously to multiple topics"));

        // Close the transport after operations are completed
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.close())
            .peek(result -> System.out.println("> Transport closed"));

        // Close asynchronously
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.closeAsync().onFailure(error -> System.err.println("Failed to close: " + error.getMessage())))
            .peek(result -> System.out.println("> Transport closed asynchronously"));
    });
}
```

## With Lockable:
```java
RabbitLockMapper lock = new RabbitLockMapper();

lock.acquire(message, input -> {
    System.out.println("Received message: " + input);
    return Either.right(null);
}).apply(message);

return Either.right(null);
```

## Expected Output sout (Console)
```
publish, subscribe topic -> "server", "server_async"
> Received message: sync message
> Received async message: async message
RabbitUser{name='Frankie', age=40}
```

**Multiple operations**
```
multi::publish, subscribe topic -> "server-1", "server-2", "server-3"
> Published message to multiple topics
> Subscribed to multiple topics

> Received message from topic: RabbitUser{name='Frankie', age=40}
> Received message from topic: RabbitUser{name='Frankie', age=40}
> Received message from topic: RabbitUser{name='Frankie', age=40}

> Published async message to multiple topics
> Subscribed asynchronously to multiple topics

> Received async message from topic: RabbitUser{name='Frankie', age=40}
> Received async message from topic: RabbitUser{name='Frankie', age=40}
> Received async message from topic: RabbitUser{name='Frankie', age=40}

> Transport closed
> Transport closed asynchronously
````

**If you are interested in exploring functional programming and its applications within this project visit the repository at [vavr-in-action](https://github.com/noyzys/bukkit-vavr-in-action), [fp-practice](https://github.com/noyzys/fp-practice).**
