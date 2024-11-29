package dev.nautchkafe.rabbit.bridge;

import io.vavr.control.Either;
import io.vavr.concurrent.Future;

/**
 * Interface for publishing messages to RabbitMQ topics.
 * This interface provides methods to publish messages synchronously and asynchronously.
 *
 * @param <TYPE> The type of the message to be published.
 */
interface RabbitPublisher<TYPE> {
    
    /**
     * Publishes a message to a specified RabbitMQ topic synchronously.
     *
     * @param topic The name of the RabbitMQ topic to which the message is published.
     * @param data  The message data to be published. It must be of type TYPE.
     * @return An Either value, where a successful result is represented as a Void, 
     *         and a failure is represented by an exception (Throwable).
     */
    Either<Throwable, Void> publish(final String topic, final TYPE data);

    /**
     * Publishes a message to a specified RabbitMQ topic asynchronously.
     *
     * @param topic   The name of the RabbitMQ topic to which the message is published.
     * @param message The message data to be published asynchronously.
     * @return A Future representing the result of the asynchronous publish operation.
     *         It will complete once the publish action is finished.
     */
    Future<Void> publishAsync(final String topic, final TYPE message);
}