package me.predefine.mixer.impl;

import me.predefine.mixer.api.Ingredient;
import me.predefine.mixer.api.Mix;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class RecipeBook {
    public static ArrayList<MixRecipe> recipes = new ArrayList<>();

    public static void addRecipe(Class clazz) throws Exception
    {
        Mix mix = null;
        for (Mix annotation : (Mix[]) clazz.getAnnotationsByType(Mix.class))
        {
            if (mix != null)
               throw new Exception("class " + clazz.getTypeName() + " should contain only one @Mix annotation");
            mix = annotation;
        }
        if (mix == null)
            throw new Exception("class " + clazz.getTypeName() + " doesn't contain @Mix annotation");

        MixRecipe recipe = new MixRecipe(mix);

        for (Method method : clazz.getDeclaredMethods())
        {
            Ingredient ingredient = method.getAnnotation(Ingredient.class);
            if (ingredient != null)
                recipe.addIngredient(ingredient, method);
        }

        recipes.add(recipe);
    }

    public static MixRecipe[] getRecipesFor(String className)
    {
        ArrayList<MixRecipe> recipesForClass = new ArrayList<>();
        recipes.forEach(recipe -> {
            if (recipe.validFor(className))
                recipesForClass.add(recipe);
        });
        return recipesForClass.toArray(new MixRecipe[0]);
    }
}
