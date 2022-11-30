package com.ottantol.neutron.functionchain;

import com.ottantol.neutron.FunctionChain;
import com.ottantol.neutron.functionchain.util.ClassHierarchy;
import com.ottantol.neutron.functionchain.util.FunctionChainRuntimeException;
import com.ottantol.neutron.functionchain.util.FunctionChainSeed;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static com.ottantol.neutron.functionchain.util.FunctionChainFactory.createFunctionChain;
import static com.ottantol.neutron.functionchain.util.FunctionChainFactory.createFunctionChainRoot;
import static org.junit.Assert.*;

public class FunctionalTests {

    @Test
    public void test_getter_chain_none_null() {
        FunctionChain<ClassHierarchy.Third> result = createFunctionChain(
                FunctionChain.SafetyLevel.NULL_CHECK,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL
        );

        assertNotNull(result);
        assertNotNull(result.get());
    }

    @Test
    public void test_getter_chain_one_null() {
        FunctionChain<ClassHierarchy.Third> result = createFunctionChain(
                FunctionChain.SafetyLevel.NULL_CHECK,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NULL,
                FunctionChainSeed.NON_NULL
        );

        assertNotNull(result);
        assertNull(result.get());
        assertTrue(result.getPartial() instanceof ClassHierarchy.First);
    }

    @Test
    public void test_getter_chain_partial_first_fails() {
        FunctionChain<ClassHierarchy.Third> result = createFunctionChain(
                FunctionChain.SafetyLevel.NULL_CHECK,
                FunctionChainSeed.NULL,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL
        );

        assertNotNull(result);
        assertNull(result.get());
        assertNotNull(result.getPartial());
        assertTrue(result.getPartial() instanceof ClassHierarchy);
    }

    @Test
    public void test_getter_chain_partial_last_fails() {
        FunctionChain<ClassHierarchy.Third> result = createFunctionChain(
                FunctionChain.SafetyLevel.NULL_CHECK,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NULL
        );

        assertNotNull(result);
        assertNull(result.get());
        assertNotNull(result.getPartial());
        assertTrue(result.getPartial() instanceof ClassHierarchy.Second);
    }

    @Test
    public void test_getter_chain_no_safety_no_null() {
        FunctionChain<ClassHierarchy.Third> functionChain = createFunctionChain(
                FunctionChain.SafetyLevel.NONE,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL
        );

        assertNotNull(functionChain);
        assertTrue(functionChain.get() instanceof ClassHierarchy.Third);
        assertTrue(functionChain.getPartial() instanceof ClassHierarchy.Third);
    }

    @Test
    public void test_getter_chain_no_safety_root_null() {
        FunctionChain<ClassHierarchy.First> functionChain = createFunctionChain(
                FunctionChain.SafetyLevel.NONE,
                FunctionChainSeed.NULL
        );

        assertThrows(NullPointerException.class, () -> functionChain.map(ClassHierarchy.First::getNull));
    }

    @Test
    public void test_getter_chain_no_safety_first_null() {
        FunctionChain<ClassHierarchy.Second> functionChain = createFunctionChain(
                FunctionChain.SafetyLevel.NONE,
                FunctionChainSeed.NULL,
                FunctionChainSeed.NON_NULL
        );

        assertThrows(NullPointerException.class, () -> functionChain.map(ClassHierarchy.Second::getNonNull));
    }

    @Test
    public void test_getter_chain_no_safety_last_null() {
        FunctionChain<ClassHierarchy.Second> functionChain = createFunctionChain(
                FunctionChain.SafetyLevel.NONE,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NULL
        );

        assertNotNull(functionChain);
        assertNull(functionChain.get());
        assertTrue(functionChain.getPartial() instanceof ClassHierarchy.Second);
    }

    @Test
    public void test_getter_chain_throw_safety_no_throw() {
        FunctionChain<ClassHierarchy.Third> functionChain = createFunctionChain(
                FunctionChain.SafetyLevel.EXCEPTION_CHECK,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL
        );

        assertNotNull(functionChain);
        assertTrue(functionChain.get() instanceof ClassHierarchy.Third);
        assertTrue(functionChain.getPartial() instanceof ClassHierarchy.Third);
    }

    @Test
    @SuppressWarnings("all")
    public void test_getter_chain_throw_safety_root_throws() {
        assertThrows(FunctionChainRuntimeException.class, () -> FunctionChain.of(new ClassHierarchy.First().emitException(), FunctionChain.SafetyLevel.NONE));
        assertThrows(FunctionChainRuntimeException.class, () -> FunctionChain.of(new ClassHierarchy.First().emitException(), FunctionChain.SafetyLevel.NULL_CHECK));
        /* Exceptions in constructors are not handled by this solution */
        assertThrows(FunctionChainRuntimeException.class, () -> FunctionChain.of(new ClassHierarchy.First().emitException(), FunctionChain.SafetyLevel.EXCEPTION_CHECK));
    }

    @Test
    public void test_getter_chain_throw_safety_first_throws() {
        assertThrows(FunctionChainRuntimeException.class, () -> createFunctionChainRoot(FunctionChain.SafetyLevel.NONE, FunctionChainSeed.THROW));
        assertThrows(FunctionChainRuntimeException.class, () -> createFunctionChainRoot(FunctionChain.SafetyLevel.NULL_CHECK, FunctionChainSeed.THROW));

        createFunctionChainRoot(FunctionChain.SafetyLevel.EXCEPTION_CHECK, FunctionChainSeed.NON_NULL);
    }

    @Test
    public void test_getter_chain_throw_safety_last_throws() {
        FunctionChain<ClassHierarchy.Third> functionChain = createFunctionChain(
                FunctionChain.SafetyLevel.EXCEPTION_CHECK,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.THROW
        );

        assertThrows(FunctionChainRuntimeException.class, () -> createFunctionChainRoot(FunctionChain.SafetyLevel.NONE, FunctionChainSeed.THROW));
        assertThrows(FunctionChainRuntimeException.class, () -> createFunctionChainRoot(FunctionChain.SafetyLevel.NULL_CHECK, FunctionChainSeed.THROW));

        assertNotNull(functionChain);
        assertNull(functionChain.get());
        assertTrue(functionChain.getPartial() instanceof ClassHierarchy.Second);
    }

    @Test
    public void test_getter_chain_null_then_throw() {
        FunctionChain<ClassHierarchy.Third> functionChain = createFunctionChain(
                FunctionChain.SafetyLevel.EXCEPTION_CHECK,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NULL,
                FunctionChainSeed.THROW
        );

        assertNull(functionChain.get());
        assertTrue(functionChain.getPartial() instanceof ClassHierarchy.First);
    }

    @Test
    public void test_getter_chain_throw_then_null() {
        createFunctionChain(
                FunctionChain.SafetyLevel.EXCEPTION_CHECK,
                FunctionChainSeed.THROW,
                FunctionChainSeed.NULL,
                FunctionChainSeed.THROW
        );

        assertThrows(FunctionChainRuntimeException.class, () -> createFunctionChain(FunctionChain.SafetyLevel.NONE,
                FunctionChainSeed.THROW,
                FunctionChainSeed.NULL));
        assertThrows(FunctionChainRuntimeException.class, () -> createFunctionChain(FunctionChain.SafetyLevel.NULL_CHECK,
                FunctionChainSeed.THROW,
                FunctionChainSeed.NULL));

        createFunctionChain(FunctionChain.SafetyLevel.EXCEPTION_CHECK,
                FunctionChainSeed.THROW,
                FunctionChainSeed.NULL);
    }

    @Test
    public void test_getter_chain_empty_and_maps() {
        FunctionChain<?> empty = FunctionChain.empty();

        assertNull(empty.get());
        assertNull(empty.getPartial());

        FunctionChain<?> mapped = empty.map(Object::toString);

        assertNull(mapped.get());
        assertNull(mapped.getPartial());

        FunctionChain<?> flatMapped = empty.flatMap(e -> FunctionChain.empty());

        assertNull(flatMapped.get());
        assertNull(flatMapped.getPartial());
    }

    @Test
    public void test_map_vs_flat_map() {
        FunctionChain<?> resultMap = FunctionChain.of(new ClassHierarchy.First())
                .map(first -> FunctionChain.of(first.getNonNull()));

        FunctionChain<?> resultFlatMap = FunctionChain.of(new ClassHierarchy.First())
                .flatMap(first -> FunctionChain.of(first.getNonNull()));

        assertTrue(resultMap.get() instanceof FunctionChain);
        assertTrue(resultFlatMap.get() instanceof ClassHierarchy.Second);
    }

    @Test
    public void test_getter_chain_map_edge_cases() {
        FunctionChain<?> any = FunctionChain.of(new ClassHierarchy.First());

        FunctionChain<?> mappedAny = any.map(e -> null);

        assertNull(mappedAny.get());
        assertTrue(mappedAny.getPartial() instanceof ClassHierarchy.First);
    }

    @Test
    public void test_safety_level_set_get_correctly() {
        FunctionChain<?> any = FunctionChain.of(new ClassHierarchy.First());
        FunctionChain<?> empty = FunctionChain.empty();
        FunctionChain<?> customNone = FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.NONE);
        FunctionChain<?> customNull = FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.NULL_CHECK);
        FunctionChain<?> customException = FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.EXCEPTION_CHECK);

        assertEquals(any.getSafetyLevel(), FunctionChain.SafetyLevel.EXCEPTION_CHECK);
        assertEquals(empty.getSafetyLevel(), FunctionChain.SafetyLevel.NULL_CHECK);
        assertEquals(customNone.getSafetyLevel(), FunctionChain.SafetyLevel.NONE);
        assertEquals(customNull.getSafetyLevel(), FunctionChain.SafetyLevel.NULL_CHECK);
        assertEquals(customException.getSafetyLevel(), FunctionChain.SafetyLevel.EXCEPTION_CHECK);
    }

    @Test
    public void test_safety_level_passing() {
        FunctionChain<?> customNone = FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.NONE)
                .map(ClassHierarchy.First::getNonNull);
        FunctionChain<?> customNull = FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.NULL_CHECK)
                .map(ClassHierarchy.First::getNonNull);
        FunctionChain<?> customerException = FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.EXCEPTION_CHECK)
                .map(ClassHierarchy.First::getNonNull);

        assertEquals(customNone.getSafetyLevel(), FunctionChain.SafetyLevel.NONE);
        assertEquals(customNull.getSafetyLevel(), FunctionChain.SafetyLevel.NULL_CHECK);
        assertEquals(customerException.getSafetyLevel(), FunctionChain.SafetyLevel.EXCEPTION_CHECK);
    }

    @Test
    public void test_to_stream_after_init() {
        Stream<ClassHierarchy.First> streamOk = FunctionChain.of(new ClassHierarchy.First()).toStream();
        Stream<?> streamEmpty = FunctionChain.empty().toStream();

        assertEquals(streamOk.count(), 1);
        assertEquals(streamEmpty.count(), 0);
    }

    @Test
    public void test_to_stream_after_chain() {
        ClassHierarchy.Third allGood = FunctionChain.of(new ClassHierarchy.First())
                .map(ClassHierarchy.First::getNonNull)
                .map(ClassHierarchy.Second::getNonNull)
                .toStream()
                .findAny()
                .orElse(null);

        Optional<ClassHierarchy.Third> streamFailAfterSecond = FunctionChain.of(new ClassHierarchy.First())
                .map(ClassHierarchy.First::getNonNull)
                .map(ClassHierarchy.Second::getNull)
                .toStream()
                .findAny();

        assertTrue(allGood instanceof ClassHierarchy.Third);
        assertTrue(streamFailAfterSecond.isEmpty());
    }

    @Test
    public void test_to_partial_stream() {
        Optional<Object> partialStreamFailAfterSecond = createFunctionChain(
                FunctionChain.SafetyLevel.NULL_CHECK,
                FunctionChainSeed.NON_NULL,
                FunctionChainSeed.NON_NULL
        )
                .toPartialStream()
                .findAny();

        assertTrue(partialStreamFailAfterSecond.isPresent());
        assertTrue(partialStreamFailAfterSecond.get() instanceof ClassHierarchy.Second);
    }

    @Test
    public void test_map_with_safety() {
        assertThrows(FunctionChainRuntimeException.class, () -> FunctionChain.of(new ClassHierarchy.First())
                .map(ClassHierarchy.First::emitException, FunctionChain.SafetyLevel.NONE));
        assertThrows(FunctionChainRuntimeException.class, () -> FunctionChain.of(new ClassHierarchy.First())
                .map(ClassHierarchy.First::emitException, FunctionChain.SafetyLevel.NULL_CHECK));

        FunctionChain.of(new ClassHierarchy.First())
                .map(ClassHierarchy.First::emitException, FunctionChain.SafetyLevel.EXCEPTION_CHECK);

        assertThrows(FunctionChainRuntimeException.class, () -> FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.NONE)
                .map(ClassHierarchy.First::emitException, FunctionChain.SafetyLevel.NULL_CHECK));
        assertThrows(FunctionChainRuntimeException.class, () -> FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.NULL_CHECK)
                .map(ClassHierarchy.First::emitException, FunctionChain.SafetyLevel.NONE));
        assertThrows(FunctionChainRuntimeException.class, () -> FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.EXCEPTION_CHECK)
                .map(ClassHierarchy.First::emitException, FunctionChain.SafetyLevel.NULL_CHECK));
        assertThrows(FunctionChainRuntimeException.class, () -> FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.EXCEPTION_CHECK)
                .map(ClassHierarchy.First::emitException, FunctionChain.SafetyLevel.NONE));
        FunctionChain.of(new ClassHierarchy.First(), FunctionChain.SafetyLevel.NONE)
                .map(ClassHierarchy.First::emitException, FunctionChain.SafetyLevel.EXCEPTION_CHECK);
    }
}
