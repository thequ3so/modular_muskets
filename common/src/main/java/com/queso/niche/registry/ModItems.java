package com.queso.niche.registry;

import com.queso.niche.Constants;
import com.queso.niche.weapon.BarrelFx;
import com.queso.niche.weapon.MaterialTier;
import com.queso.niche.weapon.ModularWeaponItem;
import com.queso.niche.weapon.PartStats;
import com.queso.niche.weapon.WeaponAbility;
import com.queso.niche.weapon.WeaponBuild;
import com.queso.niche.weapon.WeaponComponentType;
import com.queso.niche.weapon.WeaponPartItem;
import com.queso.niche.weapon.WeaponSound;
import com.queso.niche.weapon.WeaponTrail;
import com.queso.niche.weapon.WeaponType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.queso.niche.weapon.WeaponAbility.DARKNESS;
import static com.queso.niche.weapon.WeaponAbility.EXPLOSIVE;
import static com.queso.niche.weapon.WeaponAbility.FROST;
import static com.queso.niche.weapon.WeaponAbility.FRUGAL;
import static com.queso.niche.weapon.WeaponAbility.GLOWING;
import static com.queso.niche.weapon.WeaponAbility.HOMING;
import static com.queso.niche.weapon.WeaponAbility.INCENDIARY;
import static com.queso.niche.weapon.WeaponAbility.KNOCKBACK;
import static com.queso.niche.weapon.WeaponAbility.LEVITATION;
import static com.queso.niche.weapon.WeaponAbility.LIFESTEAL;
import static com.queso.niche.weapon.WeaponAbility.LIGHTNING;
import static com.queso.niche.weapon.WeaponAbility.MULTISHOT;
import static com.queso.niche.weapon.WeaponAbility.PIERCING;
import static com.queso.niche.weapon.WeaponAbility.POISON;
import static com.queso.niche.weapon.WeaponAbility.RAPID_FIRE;
import static com.queso.niche.weapon.WeaponAbility.REPEATER;
import static com.queso.niche.weapon.WeaponAbility.RICOCHET;
import static com.queso.niche.weapon.WeaponAbility.SONIC;
import static com.queso.niche.weapon.WeaponAbility.STICKY;
import static com.queso.niche.weapon.WeaponAbility.TELEPORT;
import static com.queso.niche.weapon.WeaponAbility.WEAKNESS;
import static com.queso.niche.weapon.WeaponAbility.WITHER;
import static com.queso.niche.weapon.WeaponComponentType.BARREL;
import static com.queso.niche.weapon.WeaponComponentType.BRACING;
import static com.queso.niche.weapon.WeaponComponentType.STOCK;

public final class ModItems {

    private static final Map<ResourceKey<Item>, Item> ITEMS = new LinkedHashMap<>();
    private static final Map<String, WeaponPartItem> PARTS = new LinkedHashMap<>();

    public static final Item MUSKET_BALL = register("musket_ball", Item::new);

    public static final ModularWeaponItem MUSKET =
            register("musket", props -> new ModularWeaponItem(props.stacksTo(1), WeaponType.MUSKET));

    static {
        barrel("wooden_barrel").stats(MaterialTier.WOODEN.stats(BARREL)).register();
        stock("wooden_stock").stats(MaterialTier.WOODEN.stats(STOCK)).register();
        stock("quartz_stock").reload(-6).register();
        stock("padded_stock").aimSpeed(1.5F).stability(30).register();
        stock("amethyst_stock").velocity(0.2F).reload(-3).aimSpeed(1.2F).register();
        stock("emerald_stock").velocity(0.5F).magazine(2).register();
        stock("lapis_stock").ability(FRUGAL).register();

        bracing("copper_bracing").stats(MaterialTier.COPPER.stats(BRACING)).zoomApproach(0.35F).swayDelay(15).register();
        bracing("iron_bracing").stats(MaterialTier.IRON.stats(BRACING)).zoomApproach(0.5F).swayDelay(18).register();
        bracing("golden_bracing").stats(MaterialTier.GOLDEN.stats(BRACING)).zoomApproach(5.0F).swayDelay(40).register();
        bracing("diamond_bracing").stats(MaterialTier.DIAMOND.stats(BRACING)).zoomApproach(1.4F).swayDelay(95).register();
        bracing("netherite_bracing").stats(MaterialTier.NETHERITE.stats(BRACING)).zoomApproach(2.2F).swayDelay(130).register();

        barrel("bone_barrel").damage(0.5F).velocity(0.4F).register();
        barrel("resin_barrel").damage(2.0F).velocity(0.4F).range(3.0F).ability(STICKY)
                .sound(SoundEvents.SLIME_SQUISH, 0.6F, 1.0F).register();
        barrel("blaze_barrel").damage(4.0F).velocity(0.5F).range(3.0F).ability(INCENDIARY)
                .recoil(1.9F).sound(SoundEvents.BLAZE_SHOOT, 0.5F, 1.2F).register();
        barrel("breeze_barrel").damage(2.0F).velocity(0.5F).range(4.0F).ability(KNOCKBACK, PIERCING)
                .trail(WeaponTrail.WIND).recoil(0.4F).sound(SoundEvents.WIND_CHARGE_THROW, 0.6F, 1.0F).register();
        barrel("prismarine_barrel").damage(2.0F).velocity(1.25F).range(4.0F).ability(GLOWING, RICOCHET)
                .trail(WeaponTrail.PRISMARINE).recoil(0.4F).sound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.6F, 0.8F).register();
        barrel("echo_barrel").damage(8.0F).velocity(3.8F).range(6.0F).ability(SONIC)
                .trail(WeaponTrail.SCULK).recoil(1.9F).sound(SoundEvents.SCULK_SHRIEKER_SHRIEK, 0.3F, 1.4F).register();
        barrel("shulker_barrel").damage(3.0F).velocity(5.0F).reload(20).range(6.0F).ability(REPEATER)
                .trail(WeaponTrail.SHULKER).sound(SoundEvents.SHULKER_SHOOT, 0.7F, 1.0F).register();

    }

    private static WeaponPartItem registerPart(String id, WeaponComponentType slot, PartStats stats, List<WeaponAbility> abilities) {
        return registerPart(id, slot, stats, abilities, BarrelFx.DEFAULT);
    }

    private static WeaponPartItem registerPart(String id, WeaponComponentType slot, PartStats stats,
                                               List<WeaponAbility> abilities, BarrelFx fx) {
        WeaponPartItem part = register(id, props -> new WeaponPartItem(props, slot, stats, abilities, fx));
        PARTS.put(id, part);
        return part;
    }

    private static WeaponPartItem registerBracing(String id, PartStats stats, float zoomApproach, int swayDelay) {
        WeaponPartItem part = register(id, props -> new WeaponPartItem(props, BRACING, stats, List.of(), zoomApproach, swayDelay));
        PARTS.put(id, part);
        return part;
    }

    public static PartBuilder barrel(String id) {
        return new PartBuilder(id, BARREL);
    }

    public static PartBuilder stock(String id) {
        return new PartBuilder(id, STOCK);
    }

    public static PartBuilder bracing(String id) {
        return new PartBuilder(id, BRACING);
    }

    public static final class PartBuilder {
        private final String id;
        private final WeaponComponentType slot;
        private float damage;
        private float velocity;
        private float inaccuracy;
        private float range;
        private int reload;
        private int magazine;
        private final List<WeaponAbility> abilities = new ArrayList<>();
        private WeaponTrail trail = WeaponTrail.NONE;
        private WeaponSound sound;
        private float recoil = 1.0F;
        private float zoomApproach = WeaponPartItem.DEFAULT_ZOOM_APPROACH;
        private int swayDelay = WeaponPartItem.DEFAULT_SWAY_DELAY;
        private float aimSpeed = WeaponPartItem.DEFAULT_AIM_SPEED;
        private int stability = WeaponPartItem.DEFAULT_STABILITY;

        private PartBuilder(String id, WeaponComponentType slot) {
            this.id = id;
            this.slot = slot;
        }

        public PartBuilder stats(PartStats s) {
            this.damage = s.damage();
            this.velocity = s.velocity();
            this.inaccuracy = s.inaccuracy();
            this.reload = s.reloadTicks();
            this.magazine = s.magazine();
            this.range = s.range();
            return this;
        }

        public PartBuilder damage(float v) {
            this.damage = v;
            return this;
        }

        public PartBuilder velocity(float v) {
            this.velocity = v;
            return this;
        }

        public PartBuilder inaccuracy(float v) {
            this.inaccuracy = v;
            return this;
        }

        public PartBuilder reload(int ticks) {
            this.reload = ticks;
            return this;
        }

        public PartBuilder magazine(int rounds) {
            this.magazine = rounds;
            return this;
        }

        public PartBuilder range(float v) {
            this.range = v;
            return this;
        }

        public PartBuilder ability(WeaponAbility... granted) {
            this.abilities.addAll(List.of(granted));
            return this;
        }

        public PartBuilder trail(WeaponTrail t) {
            this.trail = t;
            return this;
        }

        public PartBuilder sound(SoundEvent event, float volume, float pitch) {
            this.sound = new WeaponSound(event, volume, pitch);
            return this;
        }

        public PartBuilder recoil(float multiplier) {
            this.recoil = multiplier;
            return this;
        }

        public PartBuilder zoomApproach(float perTick) {
            this.zoomApproach = perTick;
            return this;
        }

        public PartBuilder swayDelay(int ticks) {
            this.swayDelay = ticks;
            return this;
        }

        public PartBuilder aimSpeed(float scale) {
            this.aimSpeed = scale;
            return this;
        }

        public PartBuilder stability(int ticks) {
            this.stability = ticks;
            return this;
        }

        public WeaponPartItem register() {
            PartStats stats = new PartStats(damage, velocity, inaccuracy, reload, magazine, range);
            BarrelFx fx = new BarrelFx(trail, sound, recoil);
            WeaponPartItem part = ModItems.register(id, props ->
                    new WeaponPartItem(props, slot, stats, List.copyOf(abilities), fx, zoomApproach, swayDelay,
                            aimSpeed, stability));
            PARTS.put(id, part);
            return part;
        }
    }

    private static <T extends Item> T register(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        T item = factory.apply(new Item.Properties().setId(key));
        ITEMS.put(key, item);
        return item;
    }

    public static WeaponPartItem partById(String id) {
        return PARTS.get(id);
    }

    public static ModularWeaponItem weaponFor(WeaponType type) {
        return switch (type) {
            case MUSKET -> MUSKET;
        };
    }

    public static List<Item> defaultParts(WeaponType frame) {
        return List.of(partById("wooden_barrel"), partById("wooden_stock"), partById("copper_bracing"));
    }

    public static ItemStack defaultWeaponStack(WeaponType frame) {
        ItemStack stack = new ItemStack(weaponFor(frame));
        stack.set(ModDataComponents.WEAPON_BUILD, WeaponBuild.ofParts(defaultParts(frame)));
        return stack;
    }

    public static List<ItemStack> creativeStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : ITEMS.values()) {
            if (item instanceof ModularWeaponItem weapon) {
                stacks.add(defaultWeaponStack(weapon.frame()));
            } else {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks;
    }

    public static void register(BiConsumer<ResourceKey<Item>, Item> registrar) {
        ITEMS.forEach(registrar);
    }

    private ModItems() {}
}
