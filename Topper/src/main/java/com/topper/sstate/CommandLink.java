package com.topper.sstate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Map of {@link PicoCommand}s to their {@link CommandState}s.
 * 
 * This annotation must only be applied to subclasses of
 * <code>PicoCommand</code>.
 * 
 * @author Pascal Kühnemann
 * @since 05.09.2023
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandLink {
	/**
	 * Dictates in what {@link CommandState}s an annotated {@link PicoCommand} is
	 * allowed to run.
	 */
	@NonNull
	Class<? extends CommandState>[] states() default {};
}