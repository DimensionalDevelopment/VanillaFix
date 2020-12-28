package org.dimdev.vanillafix.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Tooltip {
	String value();
}
