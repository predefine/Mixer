package me.predefine.mixer.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Ingredient {
    String value(); // method name(ex. "append", "toString", "append(Ljava/lang/String;)Ljava/lang/StringBuilder;")

    InjectionPoint point();

    int argumentIndex() default -1; // required for MODIFY_ARGUMENT
}
