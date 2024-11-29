package dev.nautchkafe.rabbit.bridge;

import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.TypeReference;
import dev.nautchkafe.rabbit.bridge.RabbitClientConnector;
import dev.nautchkafe.rabbit.bridge.RabbitPublisher;
import dev.nautchkafe.rabbit.bridge.RabbitSubscriber;
import io.vavr.Function1;
import io.vavr.control.Try;
import io.vavr.collection.Queue;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.HashMap;
import io.vavr.concurrent.Future;

import java.io.IOException;

/**
 * A transport class that handles both publishing and subscribing to RabbitMQ topics.
 * It implements both the {@link RabbitPublisher} and {@link RabbitSubscriber} interfaces
 * for synchronous and asynchronous messaging.
 *
 * @param <TOPIC> The type of the message that will be sent and received.
 */
public final class RabbitTransport<TOPIC> implements RabbitPublisher<TOPIC>, RabbitSubscriber<TOPIC> {

    private final Channel channel;
    private final ObjectMapper objectMapper;
    private final Map<String, Queue<TOPIC>> topicQueues;

    /**
     * Constructor for initializing the RabbitTransport instance with a specific RabbitMQ channel.
     *
     * @param channel The RabbitMQ channel used for communication.
     */
    public RabbitTransport(final Channel channel) {
        this.channel = channel;
        this.objectMapper = new ObjectMapper(); 
        this.topicQueues = HashMap.empty(); 
    }

    /**
     * Creates a {@link RabbitClientResource} that initializes and disposes of a {@link RabbitTransport} instance.
     *
     * @param clientConnector The RabbitClientConnector used to establish a connection.
     * @return A {@link RabbitClientResource} instance for resource management.
     */
    public static <TOPIC> RabbitClientResource<RabbitTransport<?>> createResource(final RabbitClientConnector clientConnector) {
        final Function1<Void, Try<RabbitTransport<?>>> initializer = (Void) -> 
        Try.of(() -> {
            final Channel channel = clientConnector.createChannel().get();
            return new RabbitTransport<>(channel);
        });

        final Function1<RabbitTransport<?>, Try<Void>> disposer = (resource) ->
                Try.run(() -> resource.closeAsync().get());

        return RabbitClientResource.of(initializer, disposer);
    }

    /**
     * Publishes a message to a RabbitMQ topic synchronously.
     *
     * @param topic The name of the RabbitMQ topic.
     * @param message The message to be published.
     * @return An {@link Either} representing the result of the publish operation.
     *         {@link Either#right(Void)} for success or {@link Either#left(Throwable)} for failure.
     */
    @Override
    public Either<Throwable, Void> publish(final String topic, final TOPIC message) {
        return Either.tryCatch(() -> {
            final byte[] messageBytes = serializeMessage(message);
            channel.queueDeclare(topic, true, false, false, null);
            channel.basicPublish("", topic, null, messageBytes);
        }, Throwable::getCause);
    }

    /**
     * Publishes a message to multiple topics synchronously.
     *
     * This method publishes the message to all the provided topics. For each topic, the publishing operation
     * is synchronous, meaning the operation will block until the message is published to each topic.
     *
     * @param topics A list of topic names to publish the message to.
     * @param message The message to be published to the topics.
     * @return An {@link Either} representing the result of the publish operation:
     *         {@link Either#right(Void)} for success or {@link Either#left(Throwable)} for failure.
     */
    public Either<Throwable, Void> publishMultiple(final List<String> topics, final TOPIC message) {
        return Either.tryCatch(() -> {
            final byte[] messageBytes = serializeMessage(message);

            topics.forEach(topic -> {
                channel.queueDeclare(topic, true, false, false, null);
                channel.basicPublish("", topic, null, messageBytes);
            });
        }, Throwable::getCause);
    }

    /**
     * Publishes a message to multiple topics asynchronously.
     *
     * This method publishes the message to all the provided topics asynchronously. For each topic, the publishing 
     * operation is performed asynchronously, and the method returns a {@link Future<Void>} that represents the 
     * completion of all the publishing operations.
     *
     * @param topics A list of topic names to publish the message to.
     * @param message The message to be published to the topics.
     * @return A {@link Future<Void>} representing the result of the asynchronous publish operation.
     *         The future is completed once all topics have been published to.
     */
    public Future<Void> publishMultipleAsync(final List<String> topics, final TOPIC message) {
        return Future.future(promise -> {
            Try.of(() -> {
                final byte[] messageBytes = serializeMessage(message);

                topics.forEach(topic -> {
                    Try.run(() -> {
                        channel.queueDeclare(topic, true, false, false, null);
                        channel.basicPublish("", topic, null, messageBytes);
                    }).onFailure(promise::fail);
                });
            }).onFailure(promise::fail)
            .onSuccess(v -> promise.complete(null));
        });
    }


    /**
     * Publishes a message to a RabbitMQ topic asynchronously.
     *
     * @param topic The name of the RabbitMQ topic.
     * @param message The message to be published asynchronously.
     * @return A {@link Future} representing the result of the asynchronous publish operation.
     */
    @Override
    public Future<Void> publishAsync(final String topic, final TOPIC message) {
        return Future.future(promise -> {
            Try.of(() -> {
                final byte[] messageBytes = serializeMessage(message);
                channel.queueDeclare(topic, true, false, false, null);
                channel.basicPublish("", topic, null, messageBytes);
            }).onFailure(promise::fail)
                .onSuccess(v -> promise.complete(null));
        });
    }

    /**
     * Subscribes to a RabbitMQ topic synchronously and handles incoming messages.
     *
     * @param topic The name of the RabbitMQ topic to subscribe to.
     * @param onMessage A function that defines the logic for handling each received message.
     * @return An {@link Either} representing the result of the subscribe operation.
     *         {@link Either#right(Void)} for success or {@link Either#left(Throwable)} for failure.
     */
    @Override
    public Either<Throwable, Void> subscribe(final String topic, final Function1<TOPIC, Either<Throwable, Void>> onMessage) {
        return Either.tryCatch(() -> {
            channel.queueDeclare(topic, true, false, false, null);
            final DeliverCallback callback = (consumerTag, delivery) -> {
                TOPIC message = deserializeMessage(delivery.getBody());
                inMemoryQueue = inMemoryQueue.append(message);
                onMessage.apply(message).peekLeft(error -> System.err.println("> Message handling error: " + error.getMessage()));
            };

            channel.basicConsume(topic, true, callback, consumerTag -> {});
        }, Throwable::getCause);
    }

    /**
     * Subscribes to a RabbitMQ topic asynchronously and handles incoming messages.
     *
     * @param topic The name of the RabbitMQ topic to subscribe to.
     * @param onMessage A function that defines the logic for handling each received message asynchronously.
     * @return A {@link Future} representing the result of the asynchronous subscribe operation.
     */
    @Override
    public Future<Void> subscribeAsync(final String topic, final Function1<TOPIC, Future<Void>> onMessage) {
        return Future.future(promise -> {
            Try.run(() -> channel.queueDeclare(topic, true, false, false, null))
                .onFailure(promise::fail)
                .onSuccess(v -> {
                    final DeliverCallback callback = (consumerTag, delivery) -> {
                        final Try<TOPIC> messageTry = Try.of(() -> deserializeMessage(delivery.getBody()))
                            .onFailure(error -> promise.fail(error));

                        messageTry.onSuccess(message -> {
                            final Queue<TOPIC> currentQueue = topicQueues.getOrElse(topic, Queue.empty());
                            topicQueues.put(topic, currentQueue.append(message));

                            onMessage.apply(message)
                                .onFailure(error -> System.err.println("> Błąd obsługi wiadomości: " + error.getMessage()))
                                .onComplete(result -> promise.complete(null));
                        });
                };

                Try.run(() -> channel.basicConsume(topic, true, callback, consumerTag -> {}))
                .onFailure(promise::fail);
            });
        });
    }

    /**
     * Subscribes to multiple topics and processes incoming messages concurrently.
     *
     * This method subscribes to all the provided topics and initiates message consumption for each one.
     * Each message is processed using the provided {@code onMessage} function.
     * The operation is asynchronous, and it completes once all subscriptions are handled.
     *
     * @param topics A list of topic names to subscribe to.
     * @param onMessage A function that handles each received message. The function returns a {@link Future<Void>} 
     *                  indicating that the message processing is asynchronous.
     *                  The function accepts a message of type {@code TOPIC} as its argument.
     * @return A {@link Future<Void>} representing the result of the subscription operation.
     *         {@link Future#complete(Void)} is called once all subscriptions are completed.
     */
    public Future<Void> subscribeToMultipleTopics(
        final List<String> topics, final Function1<TOPIC, Future<Void>> onMessage) {

        return Future.future(promise -> {
            topics.forEach(topic -> topicQueues.put(topic, Queue.empty()));

            final List<Future<Void>> subscriptionFutures = topics.map(topic -> subscribeAsync(topic, onMessage));

            Future.sequence(subscriptionFutures).onComplete(result -> promise.complete(null));
        });
    }

    /**
     * Closes the RabbitMQ channel synchronously.
     *
     * @return An {@link Either} representing the result of the close operation.
     *         {@link Either#right(Void)} for success or {@link Either#left(Throwable)} for failure.
     */
    public Either<Throwable, Void> close() {
        return Either.tryCatch(() -> {
            channel.close();
        }, Throwable::getCause);
    }

    /**
     * Closes the RabbitMQ channel asynchronously.
     *
     * @return A {@link Future} representing the result of the asynchronous close operation.
     */
    public Future<Void> closeAsync() {
        return Future.future(promise -> Try.run(() -> channel.close())
            .onFailure(promise::fail)
            .onSuccess(() -> promise.complete(null)));
    }

    /**
     * Serializes a message of type {@code TOPIC} into a byte array using ObjectMapper.
     *
     * @param message The message to be serialized.
     * @return The byte array representation of the message.
     * @throws IOException If an error occurs during serialization.
     */
    private byte[] serializeMessage(final TOPIC message) throws IOException {
        return objectMapper.writeValueAsBytes(message);
    }

    /**
     * Deserializes a byte array into a message of type {@code TOPIC}.
     *
     * @param messageBytes The byte array representing the serialized message.
     * @return The deserialized message of type {@code TOPIC}.
     * @throws IOException If an error occurs during deserialization.
     */
    private TOPIC deserializeMessage(final byte[] messageBytes) throws IOException {
        return objectMapper.readValue(messageBytes, new TypeReference<TOPIC>() {});
    }

    /**
     * Returns the in-memory queue of received messages.
     *
     * @return A queue containing the received messages.
     */
    public Queue<TOPIC> getInMemoryQueue() {
        return inMemoryQueue;
    }
}
