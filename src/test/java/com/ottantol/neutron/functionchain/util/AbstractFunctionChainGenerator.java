package com.ottantol.neutron.functionchain.util;

import com.ottantol.neutron.FunctionChain;

public abstract class AbstractFunctionChainGenerator<T, R> {

    public <P extends AbstractFunctionChainGenerator<T, R>> FunctionChain<R> create(FunctionChain<P> functionChain, FunctionChainSeed functionChainSeed) {
        switch(functionChainSeed) {
            case NON_NULL: return functionChain.map(AbstractFunctionChainGenerator::getNonNull);
            case THROW: return functionChain.map(AbstractFunctionChainGenerator::getException);
            case NULL:return functionChain.map(AbstractFunctionChainGenerator::getNull);
            default: return FunctionChain.of(null);
        }
    }

    abstract R getNull();

    abstract R getNonNull();

    R getException() { throw new FunctionChainRuntimeException(); }
}
