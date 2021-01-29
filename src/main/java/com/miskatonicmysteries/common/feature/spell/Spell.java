package com.miskatonicmysteries.common.feature.spell;

import com.miskatonicmysteries.common.feature.blessing.Blessing;
import com.miskatonicmysteries.common.feature.interfaces.Ascendant;
import com.miskatonicmysteries.common.handler.networking.packet.SpellPacket;
import com.miskatonicmysteries.common.lib.Constants;
import com.miskatonicmysteries.common.lib.MMMiscRegistries;
import com.miskatonicmysteries.common.lib.util.CapabilityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class Spell {
    public SpellEffect effect;
    public SpellMedium medium;
    public int intensity;

    public Spell(SpellMedium medium, SpellEffect effect, int intensity) {
        this.medium = medium;
        this.effect = effect;
        this.intensity = intensity;
    }


    public boolean cast(LivingEntity caster) {
        caster.world.playSound(caster.getX(), caster.getY(), caster.getZ(), MMMiscRegistries.Sounds.MAGIC, SoundCategory.PLAYERS, 0.85F, (float) caster.getRandom().nextGaussian() * 0.2F + 1.0F, true);
        if (!caster.world.isClient) {
            SpellPacket.send(caster, toTag(new CompoundTag()));
        }
        boolean boost = Ascendant.of(caster).isPresent() && CapabilityUtil.hasBlessing(Ascendant.of(caster).get(), Blessing.MAGIC_BOOST);
        return effect.canCast(caster, medium) && medium.cast(caster.world, caster, effect, boost ? intensity + 1 : intensity);
    }

    public CompoundTag toTag(CompoundTag tag) {
        tag.putString(Constants.NBT.SPELL_EFFECT, effect.getId().toString());
        tag.putString(Constants.NBT.SPELL_MEDIUM, medium.getId().toString());
        tag.putInt(Constants.NBT.INTENSITY, intensity);
        return tag;
    }

    public static Spell fromTag(CompoundTag tag) {
        return tag.isEmpty() ? null : new Spell(SpellMedium.SPELL_MEDIUMS.get(new Identifier(tag.getString(Constants.NBT.SPELL_MEDIUM))), SpellEffect.SPELL_EFFECTS.get(new Identifier(tag.getString(Constants.NBT.SPELL_EFFECT))), tag.getInt(Constants.NBT.INTENSITY));
    }
}
