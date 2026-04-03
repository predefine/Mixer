package me.predefine.mixer.impl;

import me.predefine.mixer.api.MixService;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;
import java.util.jar.JarFile;

public class AgentMain {
    public static void premain(String args, Instrumentation inst) throws Exception {
        System.out.println("Hello, world!");

        inst.appendToSystemClassLoaderSearch(new JarFile(args));
        ServiceLoader<MixService> mixes = ServiceLoader.load(MixService.class, new URLClassLoader(new URL[]{new File(args).toURI().toURL()}));

        for(MixService mix : mixes)
            RecipeBook.addRecipe(mix.getClass());

        inst.addTransformer((classLoader, classPath, clazz, protectionDomain, bytes) -> {
            String className = classPath.replaceAll("/", ".");
            MixRecipe[] recipes = RecipeBook.getRecipesFor(className);
            if (recipes.length == 0)
                return new byte[0];

            BytecodeMixer mixer = new BytecodeMixer(bytes);
            for (MixRecipe recipe : recipes)
                mixer.mix(recipe);
            return mixer.toBytecode();
        });
    }
}
