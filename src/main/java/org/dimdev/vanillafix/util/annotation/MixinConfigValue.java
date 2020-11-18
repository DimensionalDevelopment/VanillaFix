package org.dimdev.vanillafix.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// serves no purpose as of now

// will come in handy if i decide to use asm to
// read annotations from mixins to decide whether
// they should be applied
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MixinConfigValue {
    String category();

    String value();
}
