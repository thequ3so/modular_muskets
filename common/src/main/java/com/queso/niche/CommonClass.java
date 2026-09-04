package com.queso.niche;

import com.queso.niche.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

public class CommonClass {

    public static void init() {

        NicheConfig.load();
        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));

        if (Services.PLATFORM.isModLoaded("niche")) {

            Constants.LOG.info("Hello to niche");
        }
    }
}
