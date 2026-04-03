package me.predefine.mixer.api;

public interface IngredientContext {
    void setReturn(Object value); // change return value
    Object getReturn(); // could be overridden by setReturn in other @Ingredient call
    void cancel(); // cancel execution of real method. should be called only in @Ingredient with `point = BEFORE_IMPLEMENTATION` otherwise ignore
}
