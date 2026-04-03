package me.predefine.mixer.example;

import com.google.auto.service.AutoService;
import me.predefine.mixer.api.Ingredient;
import me.predefine.mixer.api.InjectionPoint;
import me.predefine.mixer.api.Mix;
import me.predefine.mixer.api.MixService;

import java.awt.image.BufferedImage;

@AutoService(MixService.class)
@Mix("ru.minecraftonly.launcher.gui.frames.main.UserPanel")
public class MixUserPanel implements MixService {
    @Ingredient(value = "i(Ljava/lang/String;Ljava/awt/image/BufferedImage;I)V", point = InjectionPoint.MODIFY_ARGUMENT, argumentIndex = 0)
    public static String setUserInfo__username(String username)
    {
        return "there is something you don't know";
    }

    @Ingredient(value = "i(Ljava/lang/String;Ljava/awt/image/BufferedImage;I)V", point = InjectionPoint.MODIFY_ARGUMENT, argumentIndex = 1)
    public static BufferedImage setUserInfo__skin(BufferedImage skin)
    {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    }

    @Ingredient(value = "i(Ljava/lang/String;Ljava/awt/image/BufferedImage;I)V", point = InjectionPoint.MODIFY_ARGUMENT, argumentIndex = 2)
    public static int setUserInfo__coins(int coins)
    {
        return 69420;
    }
}
