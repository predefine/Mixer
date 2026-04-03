package me.predefine.mixer.example;

import com.google.auto.service.AutoService;
import me.predefine.mixer.api.Ingredient;
import me.predefine.mixer.api.IngredientContext;
import me.predefine.mixer.api.InjectionPoint;
import me.predefine.mixer.api.Mix;
import me.predefine.mixer.api.MixService;

@AutoService(MixService.class)
@Mix("ru.minecraftonly.launcher.Servers_Server")
public class MixServers_Server implements MixService {
    @Ingredient(value = "getName", point = InjectionPoint.AFTER_IMPLEMENTATION)
    public static void getName_Hook(IngredientContext context)
    {
        context.setReturn("Mew");
    }

    @Ingredient(value = "updateStatus", point = InjectionPoint.MODIFY_ARGUMENT, argumentIndex = 0)
    public static String updateStatus_Hook(String obj)
    {
        if (!obj.equals("Offline"))
            return "69/420";
        return obj;
    }
}
