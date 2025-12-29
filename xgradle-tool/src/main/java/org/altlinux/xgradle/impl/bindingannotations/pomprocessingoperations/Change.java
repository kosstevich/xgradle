package org.altlinux.xgradle.impl.bindingannotations.pomprocessingoperations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@BindingAnnotation
@Target({FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
public @interface Change {
}
