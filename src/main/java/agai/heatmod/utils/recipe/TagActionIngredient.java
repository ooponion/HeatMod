package agai.heatmod.utils.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import java.util.stream.Stream;

import static agai.heatmod.Heatmod.MODID;

public class TagActionIngredient extends Ingredient {
	public static final TagActionIngredientSerializer SERIALIZER=new TagActionIngredientSerializer(new ResourceLocation(MODID,"tag"));
	public static record TagActionIngredientSerializer(ResourceLocation name) implements IIngredientSerializer<TagActionIngredient>{
		
		@Override
		public TagActionIngredient parse(FriendlyByteBuf buffer) {
			return new TagActionIngredient(this,ItemTags.create(buffer.readResourceLocation()));
		}

		@Override
		public TagActionIngredient parse(JsonObject json) {
			return new TagActionIngredient(this,ItemTags.create(new ResourceLocation(json.get("tag").getAsString())));
		}

		@Override
		public void write(FriendlyByteBuf buffer, TagActionIngredient ingredient) {
			buffer.writeResourceLocation(ingredient.tool.location());
			
		}
		
	}
	TagKey<Item> tool;
	TagActionIngredientSerializer serializer;
	public TagActionIngredient(TagActionIngredientSerializer serializer,TagKey<Item> tool) {
		super(Stream.of(new TagValue(tool)));
		this.tool=tool;
		this.serializer=serializer;
	}

	@Override
	public boolean test(ItemStack pStack) {
		return pStack.is(tool);
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", getSerializer().name.toString());
		json.addProperty("tag", tool.location().toString());
		return json;
	}

	@Override
	public TagActionIngredientSerializer getSerializer() {
		return serializer;
	}

}
