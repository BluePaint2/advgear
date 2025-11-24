package com.bluepaint.advgear;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(CreateAdvancedGearbox.MODID)
public class CreateAdvancedGearbox {
    public static final String MODID = "advgear";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    static {
        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register(MODID, () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> AdvancedGearbox.ADVANCED_GEARBOX.asItem().getDefaultInstance())
            .title(Component.translatable("itemGroup.advgear"))
            .displayItems((itemDisplayParameters, output) -> REGISTRATE.getAll(Registries.ITEM)
                    .forEach((item -> {
                        if (!(item.get() instanceof SequencedAssemblyItem)) {
                            output.accept(item.get());
                        }
                    }))
            )
            .build());

    public CreateAdvancedGearbox(IEventBus eventBus, ModContainer container) {
        eventBus.addListener(this::clientSetup);

        REGISTRATE.registerEventListeners(eventBus);
        AdvancedGearbox.register();
        CREATIVE_MODE_TABS.register(eventBus);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new AdvancedGearboxPonder());
    }
}
