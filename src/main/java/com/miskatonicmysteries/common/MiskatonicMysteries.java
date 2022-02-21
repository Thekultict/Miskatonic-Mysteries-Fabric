package com.miskatonicmysteries.common;

import com.miskatonicmysteries.api.banner.MMBannerRegistry;
import com.miskatonicmysteries.common.feature.ModCommand;
import com.miskatonicmysteries.common.handler.SchedulingHandler;
import com.miskatonicmysteries.common.handler.networking.packet.SpellPacket;
import com.miskatonicmysteries.common.handler.networking.packet.SyncSpellCasterDataPacket;
import com.miskatonicmysteries.common.handler.networking.packet.c2s.InvokeManiaPacket;
import com.miskatonicmysteries.common.registry.MMAffiliations;
import com.miskatonicmysteries.common.registry.MMBlessings;
import com.miskatonicmysteries.common.registry.MMCriteria;
import com.miskatonicmysteries.common.registry.MMEntities;
import com.miskatonicmysteries.common.registry.MMInsanityEvents;
import com.miskatonicmysteries.common.registry.MMLootTables;
import com.miskatonicmysteries.common.registry.MMObjects;
import com.miskatonicmysteries.common.registry.MMParticles;
import com.miskatonicmysteries.common.registry.MMRecipes;
import com.miskatonicmysteries.common.registry.MMRites;
import com.miskatonicmysteries.common.registry.MMSounds;
import com.miskatonicmysteries.common.registry.MMSpellEffects;
import com.miskatonicmysteries.common.registry.MMSpellMediums;
import com.miskatonicmysteries.common.registry.MMStatusEffects;
import com.miskatonicmysteries.common.registry.MMTrades;
import com.miskatonicmysteries.common.registry.MMWorld;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import software.bernie.geckolib3.GeckoLib;

public class MiskatonicMysteries implements ModInitializer {

	public static MMConfig config;

	static {
		AutoConfig.register(MMConfig.class, JanksonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(MMConfig.class).getConfig();
	}

	@Override
	public void onInitialize() {
		MMAffiliations.init();
		MMBlessings.init();
		MMObjects.init();
		MMEntities.init();
		MMSpellMediums.init();
		MMSpellEffects.init();
		MMRites.init();
		MMSounds.init();
		MMStatusEffects.init();
		MMLootTables.init();
		MMTrades.init();
		MMCriteria.init();
		MMRecipes.init();
		MMInsanityEvents.init();
		MMParticles.init();
		ModCommand.setup();
		MMWorld.init();
		registerPackets();
		SchedulingHandler.init();
		MMServerEvents.init();
		MMBannerRegistry.registerBanner();



         /*
        todo some natural sanity regen
        */
	}

	private void registerPackets() {
		ServerPlayNetworking.registerGlobalReceiver(InvokeManiaPacket.ID, InvokeManiaPacket::handle);
		ServerPlayNetworking.registerGlobalReceiver(SyncSpellCasterDataPacket.ID, SyncSpellCasterDataPacket::handleFromClient);
		ServerPlayNetworking.registerGlobalReceiver(SpellPacket.ID, SpellPacket::handleFromClient);
	}
}
