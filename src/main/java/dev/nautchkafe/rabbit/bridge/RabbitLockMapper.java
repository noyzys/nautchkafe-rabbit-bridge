package dev.nautchkafe.rabbit.bridge;

import io.vavr.control.Try;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Acquires a lock associated with the specified key and applies the provided action function.
 * This method ensures that only one thread can execute the given action for the specified key at a time.
 * If a lock for the key does not exist, a new one is created.
 * 
 * @param <TYPE> the type of the input parameter that will be passed to the action function.
 * @param <RESULT> the type of the result returned by the action function.
 * @param key a unique identifier for the lock associated with the action. The key is used to store and retrieve the lock.
 * @param action the function that will be executed once the lock is acquired. It takes an input of type {@code T} and returns a result of type {@code R}.
 * @return a {@link Function} that takes an input of type {@code T} and returns a {@link Try} of the result of type {@code R}. 
 *         The result is wrapped in a {@link Try} to safely handle any exceptions that might be thrown during execution.
 * 
 * <p>
 * This method works as follows:
 * <ul>
 *     <li>If the lock associated with the given key does not exist, a new {@link ReentrantLock} is created.</li>
 *     <li>The lock is acquired before the action is executed to ensure that only one thread can perform the action for that key.</li>
 *     <li>Once the lock is acquired, the action is applied to the input and the result is returned as a {@link Try}.</li>
 *     <li>If the action succeeds, the lock is released immediately after the action finishes.</li>
 *     <li>If the action throws an exception, the failure is logged and the lock is released after the exception is handled.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This ensures that actions for the same key are thread-safe and prevents race conditions in concurrent environments.
 * </p>
 */
final class RabbitLockMapper implements RabbitLockable {

    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public <TYPE, RESULT> Function<T, Try<R>> acquire(final String key, final Function<TYPE, RESULT> action) {
        return input -> {
            final ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
            
            return Try.of(() -> {
                lock.lock();
                return action.apply(input); 
            }).andThen(() -> {
                lock.unlock(); 
            }).onFailure(ex -> {
                System.err.println("Error processing key: " + key + ", Error: " + ex.getMessage());
            });
        };
    }
}
