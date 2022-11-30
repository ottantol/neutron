package com.ottantol.neutron.functionchain;

import com.ottantol.neutron.FunctionChain;
import com.ottantol.neutron.functionchain.util.ClassHierarchy;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PerformanceTests {

    @Test
    public void test_should_run_under_10ms() {

        long start = System.currentTimeMillis();

        FunctionChain<ClassHierarchy.Third> result = FunctionChain.of(new ClassHierarchy.First())
                .map(ClassHierarchy.First::getNonNull)
                .map(ClassHierarchy.Second::getNonNull);

        final long runtime = System.currentTimeMillis() - start;

        assertTrue(runtime < 10);
    }
}
