package dev.compactmods.machines.client.machine;

import dev.compactmods.machines.client.machine.render.CompactMachineRenderer;
import dev.compactmods.machines.client.machine.render.MachineShaderRenderer;
import dev.compactmods.machines.client.machine.shader.FlagShader;
import dev.compactmods.machines.client.machine.shader.MachineFlagRenderTypes;
import dev.compactmods.machines.client.machine.shader.MachineShaders;
import dev.compactmods.machines.machine.Machines;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.common.NeoForge;

public interface MachinesClient {
   static void registerEvents(IEventBus modBus) {
	  modBus.addListener(MachineColors::onBlockColors);
	  modBus.addListener(MachineColors::onItemColors);
	  modBus.addListener(MachinesClient::registerRenderers);
	  modBus.addListener(MachinesClient::registerRenderPipelines);

       NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, MachineShaderRenderer::extractPrideRenderState);
       NeoForge.EVENT_BUS.addListener(MachineShaderRenderer::afterTranslucent);
   }

   static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
	  event.registerBlockEntityRenderer(Machines.BlockEntities.MACHINE.get(), CompactMachineRenderer::new);
   }

   /**
    * Hand each machine pipeline to NeoForge so {@code RenderPipelines.PIPELINES_BY_LOCATION}
    * picks up the {@code compactmachines:pipeline/*} mappings and compiles the
    * matching shader sources on shader pack load.
    *
    * <p>The tye-dye pipeline is fixed; the flag pipelines are generated at
    * event time, one per registered {@link FlagShader},
    * each with its palette baked in via shader defines.</p>
    */
   static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
	  event.registerPipeline(MachineShaders.TYE_DYE_PIPELINE);
	  MachineFlagRenderTypes.registerAll(event);
   }
}
