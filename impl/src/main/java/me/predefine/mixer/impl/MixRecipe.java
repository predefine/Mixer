package me.predefine.mixer.impl;

import me.predefine.mixer.api.Ingredient;
import me.predefine.mixer.api.Mix;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class MixRecipe {
    public Mix mix;
    public ArrayList<Map.Entry<Ingredient, Method>> ingredients;

    public MixRecipe(Mix mix) {
        this.mix = mix;
        this.ingredients = new ArrayList<>();
    }

    public void addIngredient(Ingredient ingredient, Method method)
    {
        this.ingredients.add(new AbstractMap.SimpleImmutableEntry<>(ingredient, method));
    }

    public boolean validFor(String className)
    {
        return mix.value().equals(className);
    }

    public boolean haveIngredient(int access, String name, String descriptor, String[] exceptions) {
        for (Map.Entry<Ingredient, Method> ingredientEntry : ingredients)
        {
            Ingredient ingredient = ingredientEntry.getKey();
            if (ingredientValidFor(ingredient, access, name, descriptor, exceptions))
                return true;
        }
        return false;
    }

    private boolean ingredientValidFor(Ingredient ingredient, int access, String name, String descriptor, String[] exceptions) {
        if (ingredient.value().equals(name))
            return true;
        if (ingredient.value().equals(name + descriptor))
            return true;
        return false;
    }
}
