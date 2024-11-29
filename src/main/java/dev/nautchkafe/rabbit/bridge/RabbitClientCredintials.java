package dev.nautchkafe.rabbit.bridge;

/**
 * A record representing the connection credentials required to connect
 * to a RabbitMQ server.
 *
 * This record holds the necessary configuration details such as host,
 * port, username, and password used for establishing a connection to RabbitMQ.
 * The credentials are typically passed to a {@link RabbitClientConnector} to
 * initiate a connection to the RabbitMQ server.
*/
record RabbitClientCredintials(
    String host,
    int port,
    String username,
    String password
) {

    /**
     * Creates the default configuration for RabbitMQ connection credentials.
     * This method returns a {@link RabbitClientCredintials} instance initialized
     * with default values:
     * - Host: "localhost"
     * - Port: 5672
     * - Username: "admin"
     * - Password: "admin"
     *
     * @return A {@link RabbitClientCredintials} instance with default RabbitMQ
     *         connection credentials.
     */
    public static RabbitClientCredintials createConfig() {
        return new RabbitClientCredintials("localhost", 5672, "admin", "admin");
    }
}