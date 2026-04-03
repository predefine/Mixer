package me.predefine.mixer.example;

import com.google.auto.service.AutoService;
import me.predefine.mixer.api.Ingredient;
import me.predefine.mixer.api.IngredientContext;
import me.predefine.mixer.api.InjectionPoint;
import me.predefine.mixer.api.Mix;
import me.predefine.mixer.api.MixService;

@AutoService(MixService.class)
@Mix("ru.minecraftonly.launcher.Updater")
public class MixUpdater implements MixService {
    @Ingredient(value = "launcherHashMatches", point = InjectionPoint.BEFORE_IMPLEMENTATION)
    public static void launcherHashMatches_Hook(IngredientContext<Boolean> context)
    {
        System.out.println("Launcher hash matches is hooked :D");
        context.setReturn(true);
        context.cancel();
    }
}
