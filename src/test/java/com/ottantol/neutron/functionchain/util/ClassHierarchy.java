package com.ottantol.neutron.functionchain.util;

public class ClassHierarchy extends AbstractFunctionChainGenerator<ClassHierarchy, ClassHierarchy.First> {

    @Override
    public ClassHierarchy.First getNull() { return null; }
    @Override
    public ClassHierarchy.First getNonNull() { return new First(); }

    public static class First extends AbstractFunctionChainGenerator<First, Second> {

        @Override
        public Second getNull() { return null; }
        @Override
        public Second getNonNull() { return new Second(); }

        public First emitException() {
            throw new FunctionChainRuntimeException();
        }
    }

    public static class Second extends AbstractFunctionChainGenerator<Second, Third> {

        @Override
        public Third getNull() {  return null; }
        @Override
        public Third getNonNull() { return new Third(); }
    }

    public static class Third {

    }
}



