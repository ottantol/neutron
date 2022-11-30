package com.ottantol.neutron;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A solution which is similar to the {@link java.util.Optional},
 * but it actually handles long function chains, where
 * one step is building on the previous one, without the
 * need to null-check every step along the way.
 *
 * It is sometimes required to call a chain of functions
 * after each other, but we cannot be sure, which link of the chain
 * returns null and creates a {@link NullPointerException} in
 * the next step.
 *
 * Given the following call chain:
 *
 * Third result = baseClass.getFirst().getSecond().getThird();
 *
 * In the case above, we cannot be sure, that none of the method
 * calls return null and crates a {@link NullPointerException}.
 * To prevent this case, we could either use a long and not so
 * readable if-else tree or use the {@link java.util.Optional} class:
 *
 * Optional.of(baseClass)
 *  .map(BaseClass::getFirst)
 *  .map(FirstClass::getSecond)
 *  .map(SecondClass::getThird);
 *
 * There are 2 key differences between the {@link java.util.Optional} code above,
 * and the {@link FunctionChain} solution:
 *
 * 1, at any point the chain fails, {@link java.util.Optional} becomes {@link Optional#empty()}
 *      but {@link FunctionChain} keeps the partial value
 * 2, if the mapper function throws an exception then {@link java.util.Optional} doesn't handle it,
 *      while {@link FunctionChain} has a special {@link FunctionChain.SafetyLevel#EXCEPTION_CHECK}
 *      which actually catches any {@link Throwable} during the process
 *
 * @param <T> type of the stored value
 */
public final class FunctionChain<T> {

    /**
     * Container of every possible safety mechanism.
     */
    public enum SafetyLevel {
        /**
         * Not using any protection at all.
         * Basically the same as not using the {@link FunctionChain} solution at all.
         */
        NONE {
            @Override
            <P, R> R check(Function<P, R> func, P value) {
                return func.apply(value);
            }
        },
        /**
         * Only checks for null value and prevents a {@link NullPointerException}
         * whenever any link in the chain returns null.
         * Could be used to handle .get().get().get() calls
         * without the need to null-check the result after each step.
         */
        NULL_CHECK {
            @Override
            <P, R> R check(Function<P, R> func, P value) {
                return value != null ? func.apply(value) : null;
            }
        },
        /**
         * Catches any kind of {@link Throwable} during the execution
         * of the given {@link Function}.
         * Handles {@link NullPointerException}-s implicitly,
         * so it basically consists the {@link SafetyLevel#NULL_CHECK} as well.
         */
        EXCEPTION_CHECK {
            @Override
            <P, R> R check(Function<P, R> func, P value) {
                try {
                    return func.apply(value);
                } catch (Exception e) {
                    return null;
                }
            }
        };

        /**
         * Abstract version of the checking mechanism.
         * @param func function to run in a special environment
         * @param value input value to run the function with
         * @param <P> type of the function's input
         * @param <R> type of the function's output
         * @return result of the function, nullable
         */
        abstract <P, R> R check(Function<P, R> func, P value);
    }

    /** Currently stored link instance **/
    private T value;
    /** Stored partial value, only updates if the result function returns a non-null value **/
    private Object partialValue;
    /** Safety level of the chain **/
    private SafetyLevel safetyLevel;

    /** Completely empty instance **/
    private static final FunctionChain<?> EMPTY = new FunctionChain<>(null, null, SafetyLevel.NULL_CHECK);

    /**
     * Applies the given function on the current value
     * and creates a new {@link FunctionChain} instance
     * based on the result.
     *
     * We actually allow null to be passed to the given
     * mapper function. It is the caller's responsibility
     * to handle/check the input parameter of the function.
     * @param mapper mapper function
     * @param <R> type of the output of the mapper function
     * @return new {@link FunctionChain} instance
     */
    public <R> FunctionChain<R> map(Function<? super T, ? extends R> mapper) {
        R result = safetyLevel.check(mapper, value);
        return result == null ? createPartialChain()
                : new FunctionChain<>(result, result, this.safetyLevel);
    }

    /**
     * Similar to {@link FunctionChain#map(Function)},
     * but it accepts an extra {@link FunctionChain.SafetyLevel}
     * parameter, which is only used during the mapping procedure,
     * but it doesn't affect the original {@link FunctionChain.SafetyLevel}
     * of the chain.
     * @param mapper mapper function
     * @param safetyLevel {@link FunctionChain.SafetyLevel}
     * @param <R> type of the output of the mapper function
     * @return new {@link FunctionChain} instance
     */
    public <R> FunctionChain<R> map(Function<? super T, ? extends R> mapper, SafetyLevel safetyLevel) {
        R result = safetyLevel.check(mapper, value);
        return result == null ? createPartialChain()
                : new FunctionChain<>(result, result, this.safetyLevel);
    }

    /**
     * Uses the mapper parameter to map
     * the current value onto a new {@link FunctionChain}
     * instance. However, this new chain instance is
     * not getting wrapped into another chain.
     * @param mapper mapper function
     * @param <R> type of the output of the mapper function
     * @return new {@link FunctionChain} instance
     */
    public <R> FunctionChain<R> flatMap(Function<? super T, ? extends FunctionChain<? extends R>> mapper) {
        if (value != null) {
            @SuppressWarnings("unchecked")
            FunctionChain<R> result = (FunctionChain<R>)mapper.apply(value);
            return result;
        }
        return createPartialChain();
    }

    /**
     * Creates an empty {@link FunctionChain} instance.
     * @param <R> type of the required empty instance
     * @return empty {@link FunctionChain} instance
     */
    @SuppressWarnings("unchecked")
    public static <R> FunctionChain<R> empty() {
        return (FunctionChain<R>) EMPTY;
    }

    /**
     * Creates a {@link Stream} from the
     * currently hold value.
     * @return current value
     */
    public Stream<T> toStream() {
        return Stream.ofNullable(value);
    }

    /**
     * Creates a {@link Stream} from the
     * partial value.
     * @return partial value
     */
    public Stream<Object> toPartialStream() {
        return Stream.ofNullable(partialValue);
    }

    /**
     * Returns the current value.
     * No exception is thrown in case
     * the value is null.
     * @return current value
     */
    public T get() {
        return value;
    }

    /**
     * Returns the partial value.
     * No exception is thrown in case
     * the partial value is null.
     * @return partial value
     */
    public Object getPartial() {
        return partialValue;
    }

    /**
     * Returns the safety level.
     * @return safety level
     */
    public SafetyLevel getSafetyLevel() { return safetyLevel; }

    /**
     * Factory method.
     * @param root root object to start the chain from
     * @param <R> type of the root object
     * @return root element based function chain
     */
    public static <R> FunctionChain<R> of(R root) {
        return new FunctionChain<>(root, root, SafetyLevel.EXCEPTION_CHECK);
    }

    /**
     * Factory method.
     * @param root root object to start the chain from
     * @param safetyLevel safety level of the chain
     * @param <R> type of the root object
     * @return root element based function chain
     */
    public static <R> FunctionChain<R> of(R root, SafetyLevel safetyLevel) {
        return new FunctionChain<>(root, root, safetyLevel);
    }

    /**
     * Creates a new partial instance, which indicates
     * that the chain has failed, but we could have
     * a useable partial value.
     * However having a partial value is not guaranteed,
     * as the chain could have been initialized with a null object.
     * @param <R> type of the chain's value
     * @return new {@link FunctionChain} instance
     */
    private <R> FunctionChain<R> createPartialChain() {
        return new FunctionChain<>(null, this.partialValue, this.safetyLevel);
    }

    /**
     * Private ctor.
     * @param root root object to start the chain from
     * @param partialValue partial value
     * @param safetyLevel safety level
     */
    private FunctionChain(T root, Object partialValue, SafetyLevel safetyLevel) {
        this.value = root;
        this.partialValue = partialValue;
        this.safetyLevel = safetyLevel;
    }
}
