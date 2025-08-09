package agai.heatmod.events;


import agai.heatmod.data.temperature.recipeData.reloadListener.RecipeRebuiltHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static agai.heatmod.Heatmod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CommonRecipeReloadEvent {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
       RecipeRebuiltHandler.buildRecipeLists(event.getRecipeManager());
    }
}
