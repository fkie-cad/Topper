package com.topper.scengine.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.jdt.annotation.NonNull;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TopperCommandParser {
	@NonNull
	Class<? extends ScriptCommandParser> parent() default TopLevelCommandParser.class;
}