package dev.nautchkafe.rabbit.bridge;

/**
 * Represents a message with a specific type that is associated with a RabbitMQ topic.
 * This class encapsulates a message of a given type to be sent or received on a specific topic.
 * 
 * @param <TYPE> The type of the message contained in this class.
 */
final class RabbitMessageTopic<TYPE> {

    private final TYPE message;

    MessagePacket(TYPE message) {
        this.message = message;
    }

    public TYPE getMessage() {
        return message;
    }
}