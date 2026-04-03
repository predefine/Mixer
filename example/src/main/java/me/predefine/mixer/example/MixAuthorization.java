package me.predefine.mixer.example;

import com.google.auto.service.AutoService;
import me.predefine.mixer.api.Ingredient;
import me.predefine.mixer.api.IngredientContext;
import me.predefine.mixer.api.InjectionPoint;
import me.predefine.mixer.api.Mix;
import me.predefine.mixer.api.MixService;

@Mix("ru.minecraftonly.launcher.Authorization")
@AutoService(MixService.class)
public class MixAuthorization implements MixService {
    @Ingredient(value = "detectVirtualMachineName", point = InjectionPoint.BEFORE_IMPLEMENTATION)
    public static void detectVirtualMachineName_Hook(IngredientContext context)
    {
        System.out.println("detectVirtualMachineName is also hooked :D");
        context.setReturn(null);
        context.cancel();
    }
}
