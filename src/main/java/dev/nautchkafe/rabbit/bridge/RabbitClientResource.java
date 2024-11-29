package dev.nautchkafe.rabbit.bridge;

import io.vavr.Function1;
import io.vavr.control.Try;

/**
 * Represents a resource that can be initialized and disposed using the provided functions.
 * 
 * This class manages the lifecycle of a resource by using a provided initializer 
 * function to create the resource and a disposer function to clean it up.
 * It ensures that the resource is disposed properly after use.
 *
 * @param <TYPE> The type of the resource managed by this class.
 */
final class RabbitClientResource<TYPE> {

    private final Function1<Void, Try<TYPE>> initializer;
    private final Function1<TYPE, Try<Void>> disposer;

    private RabbitClientResource(final Function1<Void, Try<TYPE>> initializer, final Function1<TYPE, Try<Void>> disposer) {
        this.initializer = initializer;
        this.disposer = disposer;
    }

    /**
     * Creates a new instance of {@link RabbitClientResource} using the provided initializer and disposer.
     * 
     * @param initializer A function that initializes the resource.
     * @param disposer A function that disposes the resource.
     * @param <TYPE> The type of the resource.
     * @return A new instance of {@link RabbitClientResource}.
     */
    public static <TYPE> RabbitClientResource<TYPE> of(final Function1<Void, Try<T>> initializer, final Function1<TYPE, Try<Void>> disposer) {
        return new RabbitClientResource<>(initializer, disposer);
    }

    /**
     * Executes a function with the resource, ensuring that the resource is disposed properly after the function execution.
     * 
     * The resource is initialized and passed to the provided function. After the function is executed, 
     * the resource is disposed of automatically.
     *
     * @param function The function to execute with the resource.
     * @param <RESULT> The result type of the function.
     * @return The result of the function wrapped in a {@link Try}.
     */
    public <RESULT> Try<RESULT> use(final Function1<TYPE, Try<RESULT>> function) {
        return initializer.apply(null)
               .flatMap(resource -> function.apply(resource)
                        .andFinally(() -> disposer.apply(resource)));
    }
}