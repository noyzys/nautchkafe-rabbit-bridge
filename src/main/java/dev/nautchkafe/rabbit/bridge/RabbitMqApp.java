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


