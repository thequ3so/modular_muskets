package com.queso.niche.entity;

import com.mojang.serialization.Codec;
import com.queso.niche.Constants;
import com.queso.niche.network.NicheNetworking;
import com.queso.niche.registry.ModEntities;
import com.queso.niche.registry.ModItems;
import com.queso.niche.registry.ModParticles;
import com.queso.niche.weapon.Augment;
import com.queso.niche.weapon.ModularWeaponItem;
import com.queso.niche.weapon.WeaponAbility;
import com.queso.niche.weapon.WeaponTrail;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.queso.niche.weapon.WeaponAbility.DARKNESS;
import static com.queso.niche.weapon.WeaponAbility.EXPLOSIVE;
import static com.queso.niche.weapon.WeaponAbility.FROST;
import static com.queso.niche.weapon.WeaponAbility.GLOWING;
import static com.queso.niche.weapon.WeaponAbility.HOMING;
import static com.queso.niche.weapon.WeaponAbility.INCENDIARY;
import static com.queso.niche.weapon.WeaponAbility.KNOCKBACK;
import static com.queso.niche.weapon.WeaponAbility.LEVITATION;
import static com.queso.niche.weapon.WeaponAbility.LIFESTEAL;
import static com.queso.niche.weapon.WeaponAbility.LIGHTNING;
import static com.queso.niche.weapon.WeaponAbility.PIERCING;
import static com.queso.niche.weapon.WeaponAbility.POISON;
import static com.queso.niche.weapon.WeaponAbility.REPEATER;
import static com.queso.niche.weapon.WeaponAbility.RICOCHET;
import static com.queso.niche.weapon.WeaponAbility.SONIC;
import static com.queso.niche.weapon.WeaponAbility.STICKY;
import static com.queso.niche.weapon.WeaponAbility.TELEPORT;
import static com.queso.niche.weapon.WeaponAbility.WEAKNESS;
import static com.queso.niche.weapon.WeaponAbility.WITHER;

public class MusketBall extends ThrowableItemProjectile {

    private static final float DEFAULT_DAMAGE = 6.0F;
    private static final int MAX_LIFETIME = 100;

    private static final int MAX_BOUNCES = 4;
    private static final int MAX_PENETRATE = 5;

    private float damage = DEFAULT_DAMAGE;
    private int abilities;
    private WeaponTrail trail = WeaponTrail.NONE;
    private Augment.Trait augmentTrait = Augment.Trait.NONE;
    private int life;
    private int bounces;
    private int blocksBroken;
    private double flightSpeed;
    private final Set<Integer> pierced = new HashSet<>();

    public MusketBall(EntityType<? extends MusketBall> type, Level level) {
        super(type, level);
    }

    public MusketBall(Level level, LivingEntity shooter, ItemStack ammo) {
        super(ModEntities.MUSKET_BALL, shooter, level, ammo);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setAbilities(Set<WeaponAbility> set) {
        this.abilities = WeaponAbility.toMask(set);
    }

    public void setTrail(WeaponTrail trail) {
        this.trail = trail;
    }

    public void setAugmentTrait(Augment.Trait augmentTrait) {
        this.augmentTrait = augmentTrait;
    }

    private boolean has(WeaponAbility ability) {
        return WeaponAbility.inMask(this.abilities, ability);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MUSKET_BALL;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected float getAirDrag() {
        return 1.0F;
    }

    @Override
    public void tick() {
        Vec3 preTickPos = this.position();
        super.tick();
        if (this.level() instanceof ServerLevel scanLevel) {
            tripButtonsAndLevers(scanLevel, preTickPos, this.position());
        }
        if (this.level().isClientSide() || this.isRemoved()) {
            return;
        }

        ServerLevel level = (ServerLevel) this.level();
        if (this.flightSpeed <= 0.0) {
            this.flightSpeed = this.getDeltaMovement().length();
        }
        if (augmentTrait == Augment.Trait.WATERPROOF && this.isInWater()) {
            Vec3 motion = this.getDeltaMovement();
            double sp = motion.length();
            if (sp > 1.0E-4 && this.flightSpeed > 1.0E-4) {
                this.setDeltaMovement(motion.scale(this.flightSpeed / sp));
            }
        }
        if (!suppressesDefaultTrail()) {
            double ox = (this.random.nextDouble() - 0.5) * 0.15;
            double oy = (this.random.nextDouble() - 0.5) * 0.15;
            double oz = (this.random.nextDouble() - 0.5) * 0.15;
            level.sendParticles(ModParticles.MUSKETBALL_TRAIL, this.getX() + ox, this.getY() + oy, this.getZ() + oz, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (has(INCENDIARY)) {
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 2, 0.02, 0.02, 0.02, 0.0);
            if (this.life % 3 == 0) {
                float pitch = 1.0F + (this.random.nextFloat() - 0.5F) * 0.2F;
                level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.FIRE_AMBIENT, SoundSource.NEUTRAL, 0.4F, pitch);
            }
        }
        emitTrail(level);
        if (has(HOMING)) {
            steerToward(level);
        }
        if (++this.life >= MAX_LIFETIME) {
            this.discard();
        }
    }

    private boolean suppressesDefaultTrail() {
        return this.trail == WeaponTrail.SCULK || this.trail == WeaponTrail.SHULKER;
    }

    private void emitTrail(ServerLevel level) {
        if (this.trail == WeaponTrail.NONE) {
            return;
        }
        Vec3 to = this.position();
        Vec3 step = this.getDeltaMovement();
        Vec3 from = to.subtract(step);
        int samples = Math.max(1, (int) Math.ceil(step.length() / 0.5));
        for (int i = 1; i <= samples; i++) {
            double t = i / (double) samples;
            double x = from.x + (to.x - from.x) * t;
            double y = from.y + (to.y - from.y) * t;
            double z = from.z + (to.z - from.z) * t;
            switch (this.trail) {
                case WIND -> level.sendParticles(ParticleTypes.SMALL_GUST, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
                case SCULK -> level.sendParticles(ParticleTypes.SCULK_SOUL, x, y, z, 1, 0.01, 0.01, 0.01, 0.0);
                case SHULKER -> level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
                case PRISMARINE -> level.sendParticles(ParticleTypes.GLOW, x, y, z, 1, 0.01, 0.01, 0.01, 0.0);
                case NONE -> {
                }
            }
        }
    }

    private void steerToward(ServerLevel level) {
        Vec3 motion = this.getDeltaMovement();
        double speed = motion.length();
        if (speed < 1.0E-4) {
            return;
        }
        Vec3 dir = motion.scale(1.0 / speed);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0))) {
            if (candidate == this.getOwner() || !candidate.isAlive()) {
                continue;
            }
            Vec3 to = candidate.getEyePosition().subtract(this.position());
            if (dir.dot(to.normalize()) < 0.35) {
                continue;
            }
            double d = to.lengthSqr();
            if (d < bestDist) {
                bestDist = d;
                best = candidate;
            }
        }
        if (best != null) {
            Vec3 desired = best.getEyePosition().subtract(this.position()).normalize().scale(speed);
            Vec3 steered = motion.add(desired.subtract(motion).scale(0.25));
            this.setDeltaMovement(steered.normalize().scale(speed));
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity target = hitResult.getEntity();
        if (has(PIERCING) && !this.pierced.add(target.getId())) {
            return;
        }

        LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;

        boolean headshot = target instanceof LivingEntity hit
                && hitResult.getLocation().y >= hit.getEyeY() - 0.15;
        float dealt = headshot ? this.damage * 2.0F : this.damage;

        List<ItemEntity> priorDrops = has(INCENDIARY) && target instanceof LivingEntity
                && this.level() instanceof ServerLevel preLevel
                ? preLevel.getEntitiesOfClass(ItemEntity.class, target.getBoundingBox().inflate(2.0))
                : null;

        target.hurt(this.damageSources().mobProjectile(this, owner), dealt);

        if (this.level() instanceof ServerLevel level) {
            applyEntityAbilities(level, target, owner);
            if (priorDrops != null && target instanceof LivingEntity killed && killed.isDeadOrDying()) {
                smeltFreshDrops(level, killed, priorDrops);
            }
            if (augmentTrait == Augment.Trait.LIFEGIVER && owner != null
                    && target instanceof LivingEntity slain && slain.isDeadOrDying()) {
                owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1), this);
            }
            if (headshot) {
                sendHeadshotSound();
                if (this.getOwner() instanceof ServerPlayer shooter) {
                    grantAdvancement(level, shooter, "headshot");
                }
                if (has(REPEATER) && this.getOwner() instanceof Player shooter) {
                    ModularWeaponItem.instantChamber(shooter);
                }
                if (target instanceof LivingEntity killed && killed.isDeadOrDying()
                        && this.getOwner() instanceof ServerPlayer shooter) {
                    NicheNetworking.sendHeadshotKill(shooter);
                }
            } else {
                sendHitmarker();
            }
        }
    }

    private void smeltFreshDrops(ServerLevel level, LivingEntity dead, List<ItemEntity> priorDrops) {
        for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, dead.getBoundingBox().inflate(2.0))) {
            if (priorDrops.contains(drop)) {
                continue;
            }
            ItemStack original = drop.getItem();
            ItemStack smelted = smelt(level, original);
            if (smelted != original) {
                drop.setItem(smelted);
                level.sendParticles(ParticleTypes.SMOKE, drop.getX(), drop.getY() + 0.2, drop.getZ(), 3, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }

    private static ItemStack smelt(ServerLevel level, ItemStack input) {
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getServer().getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, recipeInput, level);
        if (recipe.isEmpty()) {
            return input;
        }
        ItemStack result = recipe.get().value().assemble(recipeInput);
        if (result.isEmpty()) {
            return input;
        }
        ItemStack out = result.copy();
        out.setCount(Math.min(out.getMaxStackSize(), result.getCount() * input.getCount()));
        return out;
    }

    private void applyEntityAbilities(ServerLevel level, Entity target, @Nullable LivingEntity owner) {
        Vec3 dir = this.getDeltaMovement().normalize();
        if (has(KNOCKBACK)) {
            target.push(dir.x * 1.2, 0.35, dir.z * 1.2);
        }
        if (has(SONIC)) {
            target.push(dir.x * 2.2, 0.5, dir.z * 2.2);
            target.invulnerableTime = 0;
            target.hurt(this.damageSources().mobProjectile(this, owner), 3.0F);
            level.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getEyeY(), target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (has(INCENDIARY)) {
            target.setRemainingFireTicks(100);
        }
        if (target instanceof LivingEntity living) {
            if (has(FROST)) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2), owner);
                living.setTicksFrozen(140);
            }
            if (has(POISON)) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), owner);
            }
            if (has(WITHER)) {
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0), owner);
            }
            if (has(LEVITATION)) {
                living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 80, 1), owner);
            }
            if (has(GLOWING)) {
                living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0), owner);
            }
            if (has(DARKNESS)) {
                living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0), owner);
            }
            if (has(WEAKNESS)) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1), owner);
            }
            if (has(STICKY)) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 10), owner);
                living.setDeltaMovement(Vec3.ZERO);
                living.hurtMarked = true;
            }
        }
        if (has(LIFESTEAL) && owner != null && target instanceof LivingEntity) {
            owner.heal(this.damage * 0.35F);
        }
    }

    private static void grantAdvancement(ServerLevel level, ServerPlayer player, String id) {
        AdvancementHolder holder = level.getServer().getAdvancements()
                .get(Identifier.fromNamespaceAndPath(Constants.MOD_ID, id));
        if (holder != null) {
            player.getAdvancements().award(holder, id);
        }
    }

    private void sendHitmarker() {
        if (this.getOwner() instanceof ServerPlayer shooter) {
            shooter.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ARROW_HIT),
                    SoundSource.PLAYERS,
                    shooter.getX(), shooter.getY(), shooter.getZ(),
                    0.45F, 1.4F, this.random.nextLong()));
        }
    }

    private void sendHeadshotSound() {
        if (this.getOwner() instanceof ServerPlayer shooter) {
            shooter.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ARROW_HIT_PLAYER),
                    SoundSource.PLAYERS,
                    shooter.getX(), shooter.getY(), shooter.getZ(),
                    0.9F, 1.0F, this.random.nextLong()));
        }
    }

    private boolean tryBreakBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || hardness >= 50.0F) {
            return false;
        }
        return level.destroyBlock(pos, true, this.getOwner());
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (has(PIERCING) && hitResult instanceof EntityHitResult entityHit
                && this.pierced.contains(entityHit.getEntity().getId())) {
            return;
        }
        super.onHit(hitResult);
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel level = (ServerLevel) this.level();

        if (has(RICOCHET) && hitResult.getType() == HitResult.Type.BLOCK && this.bounces < MAX_BOUNCES
                && hitResult instanceof BlockHitResult block) {
            ricochet(level, block);
            return;
        }

        if (augmentTrait == Augment.Trait.PENETRATOR && hitResult.getType() == HitResult.Type.BLOCK
                && this.blocksBroken < MAX_PENETRATE && hitResult instanceof BlockHitResult block
                && tryBreakBlock(level, block.getBlockPos())) {
            this.blocksBroken++;
            Vec3 nudge = this.getDeltaMovement().normalize().scale(0.5);
            this.setPos(this.getX() + nudge.x, this.getY() + nudge.y, this.getZ() + nudge.z);
            return;
        }

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARROW_HIT, SoundSource.NEUTRAL, 0.35F, 1.5F);
        level.broadcastEntityEvent(this, (byte) 3);
        applyImpactAbilities(level, hitResult);

        boolean pierceThrough = has(PIERCING) && hitResult.getType() == HitResult.Type.ENTITY;
        if (!pierceThrough) {
            this.discard();
        }
    }

    private void tripButtonsAndLevers(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 seg = to.subtract(from);
        double len = seg.length();
        if (len < 1.0E-6) {
            return;
        }
        Player shooter = this.getOwner() instanceof Player player ? player : null;
        int samples = Math.max(1, (int) Math.ceil(len / 0.25));
        Set<Long> handled = new HashSet<>();
        for (int i = 0; i <= samples; i++) {
            double t = i / (double) samples;
            BlockPos pos = BlockPos.containing(from.x + seg.x * t, from.y + seg.y * t, from.z + seg.z * t);
            if (!handled.add(pos.asLong())) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            boolean isButton = state.getBlock() instanceof ButtonBlock;
            boolean isLever = state.getBlock() instanceof LeverBlock;
            if (!isButton && !isLever) {
                continue;
            }
            VoxelShape shape = state.getShape(level, pos);
            if (shape.isEmpty() || shape.clip(from, to, pos) == null) {
                continue;
            }
            if (isButton && state.getBlock() instanceof ButtonBlock button) {
                if (!state.getValue(ButtonBlock.POWERED)) {
                    button.press(state, level, pos, shooter);
                }
            } else if (state.getBlock() instanceof LeverBlock lever) {
                lever.pull(state, level, pos, shooter);
            }
        }
    }

    private void ricochet(ServerLevel level, BlockHitResult block) {
        Vec3 motion = this.getDeltaMovement();
        Direction.Axis axis = block.getDirection().getAxis();
        Vec3 reflected = switch (axis) {
            case X -> new Vec3(-motion.x, motion.y, motion.z);
            case Y -> new Vec3(motion.x, -motion.y, motion.z);
            case Z -> new Vec3(motion.x, motion.y, -motion.z);
        };
        this.bounces++;
        this.setDeltaMovement(reflected.scale(0.85));
        Vec3 nudge = reflected.normalize().scale(0.1);
        this.setPos(this.getX() + nudge.x, this.getY() + nudge.y, this.getZ() + nudge.z);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARROW_HIT, SoundSource.NEUTRAL, 0.3F, 1.8F);
        level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 3, 0.05, 0.05, 0.05, 0.02);
    }

    private void applyImpactAbilities(ServerLevel level, HitResult hitResult) {
        Vec3 pos = hitResult.getLocation();
        if (has(EXPLOSIVE)) {
            level.explode(this, pos.x, pos.y, pos.z, 1.6F, Level.ExplosionInteraction.NONE);
        }
        if (has(LIGHTNING)) {
            LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
            if (bolt != null) {
                bolt.snapTo(pos.x, pos.y, pos.z);
                level.addFreshEntity(bolt);
            }
        }
        if (has(TELEPORT)) {
            Entity owner = this.getOwner();
            if (owner != null && owner.level() == level) {
                owner.teleportTo(pos.x, pos.y, pos.z);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        if (has(INCENDIARY) && hitResult instanceof BlockHitResult block) {
            igniteTnt(level, block.getBlockPos());
        }
    }

    private void igniteTnt(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.TNT)) {
            return;
        }
        level.removeBlock(pos, false);
        LivingEntity igniter = this.getOwner() instanceof LivingEntity living ? living : null;
        PrimedTnt tnt = new PrimedTnt(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter);
        tnt.setFuse(0);
        level.addFreshEntity(tnt);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; i++) {
                double vx = (this.random.nextDouble() - 0.5) * 0.2;
                double vy = (this.random.nextDouble() - 0.5) * 0.2;
                double vz = (this.random.nextDouble() - 0.5) * 0.2;
                this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), vx, vy, vz);
            }
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.02, 0.0);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Damage", Codec.FLOAT, this.damage);
        output.putInt("Abilities", this.abilities);
        output.putInt("Trail", this.trail.ordinal());
        output.putInt("Augment", this.augmentTrait.ordinal());
        output.putInt("Life", this.life);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.damage = input.read("Damage", Codec.FLOAT).orElse(DEFAULT_DAMAGE);
        this.abilities = input.getIntOr("Abilities", 0);
        int trailId = input.getIntOr("Trail", 0);
        WeaponTrail[] trails = WeaponTrail.values();
        this.trail = trailId >= 0 && trailId < trails.length ? trails[trailId] : WeaponTrail.NONE;
        int augId = input.getIntOr("Augment", 0);
        Augment.Trait[] traits = Augment.Trait.values();
        this.augmentTrait = augId >= 0 && augId < traits.length ? traits[augId] : Augment.Trait.NONE;
        this.life = input.getIntOr("Life", 0);
    }
}
