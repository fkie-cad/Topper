package com.topper.tests.utility;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNull;

public class ConditionalAsserts {

    public static final void assertIf(final boolean premise, final boolean consequence) {
    	
    	// premise => consequence is equivalent to:
    	// not premise or consequence
    	assertTrue(!premise || consequence);
    }
    
    public static final void assertIf(final boolean premise, final boolean consequence, @NonNull final String message) {
    	
    	// premise => consequence is equivalent to:
    	// not premise or consequence
    	assertTrue(!premise || consequence, message);
    }
}