package com.queso.niche.weapon;

import com.queso.niche.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Augment {

    public enum Trait { NONE, SCOPE, BURST, WATERPROOF, PENETRATOR, LIFEGIVER }

    private static final List<Augment> REGISTRY = new ArrayList<>();
    private static final Map<Item, Augment> BY_ITEM = new HashMap<>();

    public static final Augment SPYGLASS = register("spyglass", Items.SPYGLASS, "spyglass_augment", PartStats.NONE, Trait.SCOPE);
    public static final Augment COMPARATOR = register("comparator", Items.COMPARATOR, "comparator_augment", PartStats.NONE, Trait.BURST);
    public static final Augment ENDER_CHEST = register("ender_chest", Items.ENDER_CHEST, "enderchest_augment", new PartStats(0.0F, 0.0F, 0.0F, 0, 3, 0.0F), Trait.NONE);
    public static final Augment HEART_OF_THE_SEA = register("heart_of_the_sea", Items.HEART_OF_THE_SEA, "heart_of_sea_augment", PartStats.NONE, Trait.WATERPROOF);
    public static final Augment HEAVY_CORE = register("heavy_core", Items.HEAVY_CORE, "heavy_core_augment", new PartStats(6.0F, 0.0F, 0.0F, 0, 0, 0.0F), Trait.PENETRATOR);
    public static final Augment TOTEM = register("totem", Items.TOTEM_OF_UNDYING, "totem_of_undying_augment", PartStats.NONE, Trait.LIFEGIVER);
    public static final Augment NETHER_STAR = register("nether_star", Items.NETHER_STAR, "nether_star_augment", new PartStats(3.0F, 0.0F, 0.0F, -15, 2, 0.0F), Trait.NONE);

    private final String id;
    private final Item item;
    private final String layerName;
    private final PartStats stats;
    private final Trait trait;

    private Augment(String id, Item item, String layerName, PartStats stats, Trait trait) {
        this.id = id;
        this.item = item;
        this.layerName = layerName;
        this.stats = stats;
        this.trait = trait;
    }

    public static Augment register(String id, Item item, String layerName, PartStats stats, Trait trait) {
        Augment augment = new Augment(id, item, layerName, stats, trait);
        REGISTRY.add(augment);
        BY_ITEM.put(item, augment);
        return augment;
    }

    public String id() {
        return id;
    }

    public Item item() {
        return item;
    }

    public PartStats stats() {
        return stats;
    }

    public Trait trait() {
        return trait;
    }

    public String translationKey() {
        return "augment.niche." + id;
    }

    public Identifier layer() {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/weapon/augments/" + layerName + ".png");
    }

    public String layerName() {
        return layerName;
    }

    public static List<Augment> all() {
        return REGISTRY;
    }

    public static Augment forItem(Item item) {
        return item == null ? null : BY_ITEM.get(item);
    }

    public static Augment forStack(ItemStack stack) {
        return stack.isEmpty() ? null : forItem(stack.getItem());
    }

    public static boolean isAugment(ItemStack stack) {
        return forStack(stack) != null;
    }
}
