package com.miskatonicmysteries.common.entity;

import com.miskatonicmysteries.api.interfaces.Sanity;
import com.miskatonicmysteries.common.handler.InsanityHandler;
import com.miskatonicmysteries.common.handler.networking.packet.s2c.EffectParticlePacket;
import com.miskatonicmysteries.common.registry.MMEntities;
import com.miskatonicmysteries.common.util.Constants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class HallucinationEntity extends HostileEntity {
	private static final TrackedData<EntityType<?>> ENTITY = DataTracker.registerData(HallucinationEntity.class,
			MMEntities.ENTITY_TYPE_TRACKER);
	private static final TrackedData<Optional<UUID>> HALLUCINATION_TARGET =
			DataTracker.registerData(HallucinationEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

	public HallucinationEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	public HallucinationEntity(World world) {
		this(MMEntities.HALLUCINATION, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5);
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		super.onTrackedDataSet(data);
		if (data.equals(ENTITY)) {
			calculateDimensions();
		}
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new MeleeAttackGoal(this, 1, false));
		this.goalSelector.add(1, new WanderAroundGoal(this, 1.0D));
		this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 16.0F));
		this.targetSelector.add(0, new FollowTargetGoal<>(this, PlayerEntity.class, 10, false, false,
				living -> living == getHallucinationTargetPlayer()));
	}

	public EntityType<?> getEntityHallucination() {
		return this.dataTracker.get(ENTITY);
	}

	public void setEntityHallucination(EntityType<?> type) {
		this.dataTracker.set(ENTITY, type);
	}

	public Optional<UUID> getHallucinationTarget() {
		return this.dataTracker.get(HALLUCINATION_TARGET);
	}

	public void setHallucinationTarget(UUID uuid) {
		this.dataTracker.set(HALLUCINATION_TARGET, uuid == null ? Optional.empty() : Optional.of(uuid));
	}

	@Nullable
	public PlayerEntity getHallucinationTargetPlayer() {
		Optional<UUID> uuid = getHallucinationTarget();
		return uuid.map(value -> world.getPlayerByUuid(value)).orElse(null);
	}

	@Override
	public EntityDimensions getDimensions(EntityPose pose) {
		EntityType<?> entityHallucination = getEntityHallucination();
		return entityHallucination.getDimensions();
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound tag) {
		super.readCustomDataFromNbt(tag);
		if (tag.contains(Constants.NBT.HALLUCINATION)) {
			setEntityHallucination(Registry.ENTITY_TYPE.get(new Identifier(tag.getString(Constants.NBT.HALLUCINATION))));
		}
	}

	@Override
	public void emitGameEvent(GameEvent event, @Nullable Entity entity, BlockPos pos) {
		//nuh-uh
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound tag) {
		super.writeCustomDataToNbt(tag);
		if (getEntityHallucination() != null) {
			tag.putString(Constants.NBT.HALLUCINATION,
					Registry.ENTITY_TYPE.getId(getEntityHallucination()).toString());
		}
	}

	@Override
	public boolean isInvisibleTo(PlayerEntity player) {
		PlayerEntity hallucinationTarget = getHallucinationTargetPlayer();
		return hallucinationTarget == null || player != hallucinationTarget;
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(ENTITY, EntityType.PIG);
		this.dataTracker.startTracking(HALLUCINATION_TARGET, Optional.empty());
	}

	@Override
	protected Text getDefaultName() {
		EntityType<?> hallucination = getEntityHallucination();
		return hallucination != null ? hallucination.getName() : super.getDefaultName();
	}

	@Override
	public void setTarget(@Nullable LivingEntity target) {
		if (getHallucinationTarget().isPresent() && target != null && !target.getUuid().equals(getHallucinationTarget().get())) {
			return;
		}
		super.setTarget(target);
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		return damageSource.getAttacker() != null && getHallucinationTargetPlayer() != damageSource.getAttacker();
	}

	@Override
	protected void updatePostDeath() {
		super.updatePostDeath();
	}

	@Override
	public void handleStatus(byte status) {
		if (status == 60 && world.isClient){
			if (MinecraftClient.getInstance().player == getHallucinationTargetPlayer()) {
				for (int i = 0; i < 3; ++i) {
					this.world.addParticle(ParticleTypes.LARGE_SMOKE, this.getParticleX(0.5D), this.getRandomBodyY(),
							this.getParticleZ(0.5D), 0.0D, 0.0D, 0.0D);
				}
			}
			return;
		}
		super.handleStatus(status);
	}

	@Override
	public void remove(RemovalReason reason) {
		if (reason == RemovalReason.DISCARDED) {
			this.world.sendEntityStatus(this, (byte)60);
		}
		super.remove(reason);
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (!world.isClient && source.getAttacker() != null && getHallucinationTargetPlayer() == source.getAttacker()) {
			remove(RemovalReason.DISCARDED);
		}
		return super.damage(source, amount);
	}

	@Override
	public boolean tryAttack(Entity target) {
		if (super.tryAttack(target)) {
			PlayerEntity playerTarget = getHallucinationTargetPlayer();
			if (!world.isClient && target == playerTarget) {
				float factor = InsanityHandler.calculateSanityFactor(Sanity.of(playerTarget));
				if (random.nextFloat() + 0.1F < factor) {
					remove(RemovalReason.DISCARDED);
				}
			}
			return true;
		}
		return false;
	}
}
