package com.ottantol.neutron.functionchain.util;

import com.ottantol.neutron.FunctionChain;

public final class FunctionChainFactory {

    public static FunctionChain<? extends AbstractFunctionChainGenerator<?,?>> createFunctionChainRoot(FunctionChain.SafetyLevel safetyLevel, FunctionChainSeed seed) {
        return new ClassHierarchy().create(FunctionChain.of(new ClassHierarchy(), safetyLevel), seed);
    }

    @SuppressWarnings("all")
    public static <T> FunctionChain<T> createFunctionChain(FunctionChain.SafetyLevel level, FunctionChainSeed rootSeed, FunctionChainSeed... seeds) {
        FunctionChain<? extends AbstractFunctionChainGenerator> current = createFunctionChainRoot(level, rootSeed);

        for (FunctionChainSeed seed: seeds) {
            final FunctionChain<? extends AbstractFunctionChainGenerator> finalCurrent = current;
            current = current.flatMap(e -> e.create(finalCurrent, seed));
        }

        return (FunctionChain<T>)current;
    }

    private FunctionChainFactory() {}
}
