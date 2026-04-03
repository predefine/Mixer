package me.predefine.mixer.api;

public interface IngredientContext {
    void setReturn(Object value); // change return value
    void cancel(); // cancel execution of real method
}
