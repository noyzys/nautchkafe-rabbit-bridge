package dev.nautchkafe.rabbit.bridge;

import io.vavr.Function1;
import io.vavr.concurrent.Future;
import io.vavr.control.Try;

public final class RabbitMqApp {

    public static void main(String[] args) {

        final RabbitClientConnector clientConnector = new RabbitClientConnector();
        final RabbitClientResource<RabbitTransport<RabbitUser>> resource = RabbitTransport.createResource(clientConnector);

        resource.use(transport -> {
            transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
                    .peek(result -> result.publish("server", "sync message"))
                    .peek(result -> result.subscribe("server", message -> {
                        
                        System.out.println("Received message: " + message);
                        return Either.right(null);
                    
                    })).peek(result -> result.close());

            transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
                    .peek(result -> result.publishAsync("server_async", "async message"))
                    .peek(result -> result.subscribeAsync("server_async", message -> {
                        
                        System.out.println("Received async message: " + message);
                        return Future.successful(null);

            })).peek(result -> result.closeAsync().onFailure(error -> System.err.println("Failed to close: " + error.getMessage())));
        });
    }
}



public static void main(String[] args) {

    RabbitClientConnector clientConnector = new RabbitClientConnector();
    RabbitClientResource<RabbitTransport<RabbitUser>> resource = RabbitTransport.createResource(clientConnector);

    resource.use(transport -> {
        // List of topics to subscribe to and publish to
        List<String> topics = List.of("topic1", "topic2", "topic3");

        // Example RabbitUser message
        RabbitUser userMessage = new RabbitUser("Frankie", 40);

        // Publish a message to multiple topics synchronously
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.publishMultiple(topics, userMessage))
            .peek(result -> System.out.println("Published message to multiple topics"));

        // Subscribe to multiple topics asynchronously
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.subscribeToMultipleTopics(topics, message -> {
            
            System.out.println("Received message from topic: " + message);
            return Future.successful(null); // Handle asynchronously
        })).peek(result -> System.out.println("Subscribed to multiple topics"));

        // Publish a message asynchronously to multiple topics
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.publishMultipleAsync(topics, userMessage))
            .peek(result -> System.out.println("Published async message to multiple topics"));

        // Subscribe to multiple topics asynchronously with async handling
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.subscribeToMultipleTopics(topics, message -> {
                System.out.println("Received async message from topic: " + message);
                return Future.successful(null); // Handle asynchronously

        })).peek(result -> System.out.println("Subscribed asynchronously to multiple topics"));

        // Close the transport after operations are completed
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.close())
            .peek(result -> System.out.println("Transport closed"));

        // Close asynchronously
        transport.peekLeft(error -> System.err.println("Connection failed: " + error.getMessage()))
            .peek(result -> result.closeAsync().onFailure(error -> System.err.println("Failed to close: " + error.getMessage())))
            .peek(result -> System.out.println("Transport closed asynchronously"));
    });
}
