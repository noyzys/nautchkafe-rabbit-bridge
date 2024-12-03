package dev.nautchkafe.rabbit.bridge;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.vavr.control.Either;

/**
 * A class responsible for establishing a connection to a RabbitMQ server
 * using the provided connection credentials, and creating a new RabbitMQ
 * channel for communication.
 * 
 * This class encapsulates the RabbitMQ connection logic and simplifies
 * the process of connecting to RabbitMQ by managing the connection 
 * lifecycle and providing a method to create a communication channel.
 */
final class RabbitClientConnector {

    private final RabbitClientCredentials credentials;

    /**
     * Constructs a new instance of RabbitClientConnector with default
     * RabbitMQ connection credentials.
     * The credentials are initialized using {@link RabbitClientCredentials#createConfig()}.
     */
    public RabbitClientConnector() {
        this.credintials = RabbitClientCredentials.createConfig();
    }

    /**
     * Creates a new RabbitMQ channel using the provided connection credentials.
     * This method attempts to establish a connection to the RabbitMQ server
     * using the specified credentials and returns a channel if the connection
     * is successful.
     * 
     * @return An {@link Either} object, which contains the result of the 
     *         operation. If the connection is successful, it contains a 
     *         {@link Channel} object representing the newly created channel. 
     *         If an error occurs, it contains the thrown {@link Throwable}.
     */
    public Either<Throwable, Channel> createChannel() {
        return Either.tryCatch(() -> {
            final ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(credintials.host());
            factory.setPort(credintials.port());
            factory.setUsername(credintials.username());
            factory.setPassword(credintials.password());
            
            final Connection connection = factory.newConnection();
            return connection.createChannel(); 
        }, Throwable::getCause); 
    }
}