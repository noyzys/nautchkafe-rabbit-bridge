package dev.nautchkafe.rabbit.bridge;

import io.vavr.Function1;
import io.vavr.control.Either;
import io.vavr.concurrent.Future;

/**
 * Interface for subscribing to RabbitMQ topics and handling incoming messages.
 * This interface provides methods to subscribe to topics synchronously and asynchronously.
 *
 * @param <TYPE> The type of the message that will be received from the topic.
 */
interface RabbitSubscriber<TYPE> {

    /**
     * Subscribes to a specified RabbitMQ topic and processes incoming messages synchronously.
     *
     * @param topic      The name of the RabbitMQ topic to subscribe to.
     * @param onMessage  A function that defines the logic for handling the received message.
     *                  It takes a message of type TYPE and returns an Either value, 
     *                  indicating success (Void) or failure (Throwable).
     * @return An Either value, where a successful result is represented as a Void,
     *         and a failure is represented by an exception (Throwable).
     */
    Either<Throwable, Void> subscribe(final String topic, final Function1<TYPE, Either<Throwable, Void>> onMessage);

    /**
     * Subscribes to a specified RabbitMQ topic and processes incoming messages asynchronously.
     *
     * @param topic      The name of the RabbitMQ topic to subscribe to.
     * @param onMessage  A function that defines the logic for handling the received message.
     *                  It takes a message of type TYPE and returns a Future that represents 
     *                  the asynchronous result of the operation.
     * @return A Future representing the result of the asynchronous subscribe operation.
     *         It will complete once the subscribe action is finished.
     */
    Future<Void> subscribeAsync(final String topic, final Function1<TYPE, Future<Void>> onMessage);
}