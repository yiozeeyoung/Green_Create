package com.aybeering.greencreate;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GCreate.MODID);

    // 安山合金物品
    public static final DeferredItem<Item> ANDESITE_ALLOY = ITEMS.registerSimpleItem("andesite_alloy");

    public static void register() {
        // 注册方法，由主类调用
    }
}
