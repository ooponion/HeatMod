package agai.heatmod.events;

import agai.heatmod.data.temperature.recipeData.reloadListener.RecipeReloadListener;
import net.minecraft.server.ReloadableServerResources;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static agai.heatmod.Heatmod.MODID;

@Mod.EventBusSubscriber(modid = MODID,bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AddRecipeReloadListenerEvent {
    @SubscribeEvent
    public static void addReloadListenersLowest(AddReloadListenerEvent event) {
        ReloadableServerResources dataPackRegistries = event.getServerResources();
        event.addListener(new RecipeReloadListener(dataPackRegistries));
    }
}
