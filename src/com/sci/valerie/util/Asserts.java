package com.sci.valerie.util;

public final class Asserts {
    @SafeVarargs
    public static <T> void assertIs(final String message, final T value, final T... possibilities) {
        for(final T possibility : possibilities) {
            if(value == possibility) {
                return;
            }
        }
        throw new RuntimeException(message);
    }

    @SafeVarargs
    public static <T> void assertIsNot(final String message, final T value, final T... possibilities) {
        for(final T possibility : possibilities) {
            if(value == possibility) {
                throw new RuntimeException(message);
            }
        }
    }

    private Asserts() {
    }
}