package com.queso.niche.registry;

import com.queso.niche.Constants;
import com.queso.niche.block.WeaponStationMenu;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final Identifier WEAPON_STATION_ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "weapon_station");

    public static MenuType<WeaponStationMenu> WEAPON_STATION;

    private ModMenus() {}
}
