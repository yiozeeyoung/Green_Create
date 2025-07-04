package com.aybeering;

import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class AllItems {
    private static final CreateRegistrate REGISTRATE = Create.registrate();
    
    public static final ItemEntry<Item> ANDESITE_ALLOY = taggedIngredient("andesite_alloy", AllTags.AllItemTags.CREATE_INGOTS.tag);




	@SafeVarargs
	private static ItemEntry<Item> taggedIngredient(String name, TagKey<Item>... tags) {
		return REGISTRATE.item(name, Item::new)
			.tag(tags)
			.register();
	}    
}

