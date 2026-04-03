package me.predefine.mixer.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Ingredient {
    public String value(); // method name(ex. "append", "toString")

    public InjectionPoint point();
}
