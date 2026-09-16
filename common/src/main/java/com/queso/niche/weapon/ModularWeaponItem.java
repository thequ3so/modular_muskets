package com.queso.niche.weapon;

import com.queso.niche.NicheConfig;
import com.queso.niche.client.AimClient;
import com.queso.niche.entity.MusketBall;
import com.queso.niche.registry.ModDataComponents;
import com.queso.niche.registry.ModItems;
import com.queso.niche.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModularWeaponItem extends ProjectileWeaponItem {

    private final WeaponType frame;

    private static final java.util.Map<java.util.UUID, Integer> BURST_TICKS = new java.util.HashMap<>();
    private static final int BURST_DELAY = 3;
    private static final float FRUGAL_CHANCE = 0.25F;

    public ModularWeaponItem(Properties properties, WeaponType frame) {
        super(properties);
        this.frame = frame;
    }

    public WeaponType frame() {
        return frame;
    }

    public WeaponStats stats(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY).computeStats(frame);
    }

    public static boolean isLoaded(ItemStack stack) {
        return !stack.getOrDefault(ModDataComponents.LOADED_AMMO, ChargedProjectiles.EMPTY).isEmpty();
    }

    public static int loadedCount(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.LOADED_AMMO, ChargedProjectiles.EMPTY).items().size();
    }

    private static void setLoaded(ItemStack stack, List<ItemStack> drawn) {
        stack.set(ModDataComponents.LOADED_AMMO, ChargedProjectiles.ofNonEmpty(drawn));
        setModelLoaded(stack, true);
    }

    public static void instantChamber(Player player) {
        if (!(player.getMainHandItem().getItem() instanceof ModularWeaponItem musket)) {
            return;
        }
        ItemStack weapon = player.getMainHandItem();
        int capacity = musket.effectiveMagazine(weapon);
        ChargedProjectiles loaded = weapon.getOrDefault(ModDataComponents.LOADED_AMMO, ChargedProjectiles.EMPTY);
        List<ItemStack> chamber = new ArrayList<>(loaded.itemCopies());
        if (chamber.size() >= capacity) {
            return;
        }
        ItemStack round = consumeOneAmmo(player);
        if (round.isEmpty()) {
            return;
        }
        chamber.add(round);
        setLoaded(weapon, chamber);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 0.7F, 1.8F);
    }

    public Set<WeaponAbility> abilities(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY).abilities();
    }

    private static final float DAMAGE_RECOIL_INFLUENCE = 0.6F;

    public float effectiveRecoil(ItemStack stack) {
        return recoilFor(stack.getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY), stats(stack).damage());
    }

    private float recoilFor(WeaponBuild build, float damage) {
        float base = frame.base().damage();
        float powerFactor = base <= 0.0F ? 1.0F : 1.0F + (damage / base - 1.0F) * DAMAGE_RECOIL_INFLUENCE;
        return build.recoil() * Math.max(0.3F, powerFactor);
    }

    private static void setModelLoaded(ItemStack stack, boolean loaded) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(), List.of(loaded), List.of(), List.of()));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isLoaded(stack)) {
            if (level instanceof ServerLevel serverLevel) {
                fire(serverLevel, player, hand, stack);
            } else {
                predictFire(stack);
                AimClient.addRecoil(effectiveRecoil(stack));
            }
            return InteractionResult.CONSUME;
        }
        if (hasAmmo(player)) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.PLAYERS, 0.6F, 1.7F);
        }
        return InteractionResult.FAIL;
    }

    private static boolean hasAmmo(Player player) {
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(ModItems.MUSKET_BALL)) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack consumeOneAmmo(LivingEntity entity) {
        if (entity instanceof Player player && !player.hasInfiniteMaterials()) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack slot = inventory.getItem(i);
                if (slot.is(ModItems.MUSKET_BALL)) {
                    slot.shrink(1);
                    return new ItemStack(ModItems.MUSKET_BALL);
                }
            }
            return ItemStack.EMPTY;
        }
        return new ItemStack(ModItems.MUSKET_BALL);
    }

    private int effectiveMagazine(ItemStack stack) {
        return effectiveMagazine(stack.getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY), frame);
    }

    public static int effectiveMagazine(WeaponBuild build, WeaponType frame) {
        Augment augment = build.augment();
        if (augment != null && augment.trait() == Augment.Trait.BURST) {
            return 3;
        }
        return Math.max(1, build.computeStats(frame).magazine());
    }

    private static List<ItemStack> consumeAmmo(LivingEntity entity, int capacity) {
        List<ItemStack> rounds = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            ItemStack round = consumeOneAmmo(entity);
            if (round.isEmpty()) {
                break;
            }
            rounds.add(round);
        }
        return rounds;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (level.isClientSide() || isLoaded(stack)) {
            return;
        }
        WeaponStats stats = stats(stack);
        int reloadTicks = stats.reloadTicks();
        if (abilities(stack).contains(WeaponAbility.RAPID_FIRE)) {
            reloadTicks = Math.max(1, reloadTicks / 2);
        }
        int elapsed = getUseDuration(stack, entity) - ticksRemaining;
        if (elapsed >= reloadTicks) {
            List<ItemStack> rounds = consumeAmmo(entity, effectiveMagazine(stack));
            if (!rounds.isEmpty()) {
                setLoaded(stack, rounds);
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 0.8F, 1.4F);
            }
        }
    }

    private static void predictFire(ItemStack stack) {
        List<ItemStack> chamber = new ArrayList<>(
                stack.getOrDefault(ModDataComponents.LOADED_AMMO, ChargedProjectiles.EMPTY).itemCopies());
        if (chamber.isEmpty()) {
            return;
        }
        chamber.removeFirst();
        if (chamber.isEmpty()) {
            stack.set(ModDataComponents.LOADED_AMMO, ChargedProjectiles.EMPTY);
            setModelLoaded(stack, false);
        } else {
            stack.set(ModDataComponents.LOADED_AMMO, ChargedProjectiles.ofNonEmpty(chamber));
        }
    }

    private void fire(ServerLevel level, Player player, InteractionHand hand, ItemStack stack) {
        ChargedProjectiles loaded = stack.getOrDefault(ModDataComponents.LOADED_AMMO, ChargedProjectiles.EMPTY);
        if (loaded.isEmpty()) {
            return;
        }
        List<ItemStack> chamber = new ArrayList<>(loaded.itemCopies());

        WeaponBuild build = stack.getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY);
        WeaponStats stats = stats(stack);
        Set<WeaponAbility> abilities = NicheConfig.abilitiesEnabled() ? abilities(stack) : Set.of();
        WeaponTrail trail = build.trail();
        Augment augment = build.augment();
        Augment.Trait augTrait = augment != null ? augment.trait() : Augment.Trait.NONE;

        float inaccuracy = augTrait == Augment.Trait.SCOPE && player.isShiftKeyDown()
                ? 0.0F : aimAdjustedInaccuracy(player, stats.inaccuracy(), build.swayDelay());

        boolean burst = augTrait == Augment.Trait.BURST;
        boolean multishot = !burst && abilities.contains(WeaponAbility.MULTISHOT);
        int pellets = multishot ? 5 : 1;
        float spread = multishot ? 7.0F : 0.0F;
        Vec3 look = player.getLookAngle();

        ItemStack round = chamber.getFirst().copy();
        boolean frugal = abilities.contains(WeaponAbility.FRUGAL) && level.getRandom().nextFloat() < FRUGAL_CHANCE;
        if (!frugal) {
            chamber.removeFirst();
        }
        for (int i = 0; i < pellets; i++) {
            float angle = pellets == 1 ? 0.0F : (i - (pellets - 1) / 2.0F) * spread;
            MusketBall ball = new MusketBall(level, player, round);
            ball.setDamage(stats.damage());
            ball.setAbilities(abilities);
            ball.setTrail(trail);
            ball.setAugmentTrait(augTrait);
            ball.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, stats.velocity(), inaccuracy);
            level.addFreshEntity(ball);
        }

        if (chamber.isEmpty()) {
            stack.set(ModDataComponents.LOADED_AMMO, ChargedProjectiles.EMPTY);
            setModelLoaded(stack, false);
        } else {
            stack.set(ModDataComponents.LOADED_AMMO, ChargedProjectiles.ofNonEmpty(chamber));
        }

        if (burst && !chamber.isEmpty()) {
            BURST_TICKS.put(player.getUUID(), BURST_DELAY);
        } else {
            BURST_TICKS.remove(player.getUUID());
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8F, 1.6F);
        if (frugal) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.6F);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.inventoryMenu.sendAllDataToRemote();
            }
        }
        WeaponSound barrelSound = build.fireSound();
        if (barrelSound != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    barrelSound.event(), SoundSource.PLAYERS, barrelSound.volume(), barrelSound.pitch());
        }
        double barrelLength = 1.4;
        Vec3 muzzle = player.getEyePosition().add(look.scale(barrelLength));
        for (int i = 0; i < 6; i++) {
            level.sendParticles(ModParticles.MUSKET_POOF, muzzle.x, muzzle.y, muzzle.z, 0, look.x, look.y, look.z, 0.07);
        }

        double bump = 0.07 * recoilFor(build, stats.damage()) * NicheConfig.recoilScale();
        player.setDeltaMovement(player.getDeltaMovement().add(-look.x * bump, 0.0, -look.z * bump));
        player.hurtMarked = true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (slot == EquipmentSlot.MAINHAND) {
            AimServer.update(player, player.isShiftKeyDown());
            tickBurst(level, player, stack);
        } else if (slot == EquipmentSlot.OFFHAND) {
            player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            if (player.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                player.setItemSlot(EquipmentSlot.MAINHAND, stack);
            } else if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    private void tickBurst(ServerLevel level, Player player, ItemStack stack) {
        Integer cooldown = BURST_TICKS.get(player.getUUID());
        if (cooldown == null) {
            return;
        }
        if (!isBurst(stack) || !isLoaded(stack)) {
            BURST_TICKS.remove(player.getUUID());
            return;
        }
        if (cooldown <= 1) {
            BURST_TICKS.remove(player.getUUID());
            fire(level, player, InteractionHand.MAIN_HAND, stack);
        } else {
            BURST_TICKS.put(player.getUUID(), cooldown - 1);
        }
    }

    private static boolean isBurst(ItemStack stack) {
        Augment augment = stack.getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY).augment();
        return augment != null && augment.trait() == Augment.Trait.BURST;
    }

    private static float aimAdjustedInaccuracy(Player player, float base, int swayDelay) {
        if (!player.isShiftKeyDown()) {
            return base;
        }
        int aim = AimServer.aimTicks(player);
        if (aim <= swayDelay) {
            return base * 0.25F;
        }
        float over = Mth.clamp((aim - swayDelay) / (float) Aiming.SWAY_RAMP_TICKS, 0.0F, 1.0F);
        return base * (0.25F + over * 2.25F);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ammo -> ammo.is(ModItems.MUSKET_BALL);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        MusketBall ball = new MusketBall(level, shooter, ammo);
        ball.setDamage(stats(weapon).damage());
        return ball;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
                                   float velocity, float uncertainty, float angle, @Nullable LivingEntity target) {
        projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + angle, 0.0F, velocity, uncertainty);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        return isLoaded(stack);
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return true;
    }

    private static boolean isShiftDown() {
        com.mojang.blaze3d.platform.Window window = net.minecraft.client.Minecraft.getInstance().getWindow();
        return com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT)
                || com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        WeaponBuild build = stack.getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY);
        List<WeaponPartItem> parts = build.resolveParts();
        if (!parts.isEmpty()) {
            lines.accept(Component.translatable("tooltip.niche.weapon.components").withStyle(ChatFormatting.GOLD));
            for (WeaponPartItem part : parts) {
                lines.accept(Component.literal(" • ")
                        .append(Component.translatable(part.getDescriptionId()))
                        .withStyle(ChatFormatting.GRAY));
            }
            Augment augment = build.augment();
            if (augment != null) {
                lines.accept(Component.literal(" • ")
                        .append(new ItemStack(augment.item()).getHoverName())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        if (!isShiftDown()) {
            lines.accept(Component.translatable("tooltip.niche.weapon.details").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        lines.accept(isLoaded(stack)
                ? Component.translatable("tooltip.niche.weapon.loaded").withStyle(ChatFormatting.GREEN)
                : Component.translatable("tooltip.niche.weapon.unloaded").withStyle(ChatFormatting.RED));
        lines.accept(Component.translatable("tooltip.niche.weapon.hint").withStyle(ChatFormatting.DARK_GRAY));
        WeaponTooltips.appendWeaponStats(stats(stack), lines);

        java.util.Set<WeaponAbility> abilities = build.abilities();
        if (!abilities.isEmpty()) {
            lines.accept(Component.translatable("tooltip.niche.weapon.abilities").withStyle(ChatFormatting.GOLD));
            for (WeaponAbility ability : abilities) {
                lines.accept(Component.literal("✦ ")
                        .append(Component.translatable(ability.translationKey()))
                        .withStyle(ability.color()));
            }
        }
    }
}
