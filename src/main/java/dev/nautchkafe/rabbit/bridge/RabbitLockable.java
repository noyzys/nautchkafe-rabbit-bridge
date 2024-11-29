package dev.nautchkafe.rabbit.bridge;

import io.vavr.control.Try;
import java.util.function.Function;

/**
 * This interface defines a contract for acquiring a lock associated with a specific key and executing an action.
 * Implementations of this interface ensure that the action can be safely executed with exclusive access to a given resource, identified by the key.
 * 
 * @param <TYPE> the type of the input parameter that will be passed to the action function.
 * @param <RESULT> the type of the result returned by the action function.
 */
interface RabbitLockable {

	<TYPE, RESULT> Function<TYPE, Try<RESULT>> acquire(final String key, final Function<TYPE, RESULT> action);
}