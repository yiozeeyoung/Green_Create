# ANDESITE_ALLOY 完整实现指南

本指南将帮助你在自己的 Minecraft 模组项目中完全复制 Create 模组中 `ANDESITE_ALLOY` 的所有功能和用法。

## 📋 目录
- [项目结构设置](#项目结构设置)
- [依赖配置](#依赖配置)
- [标签系统实现](#标签系统实现)
- [物品注册实现](#物品注册实现)
- [数据生成](#数据生成)
- [配方系统](#配方系统)
- [本地化文件](#本地化文件)
- [纹理资源](#纹理资源)
- [测试验证](#测试验证)
- [常见问题解决](#常见问题解决)
- [扩展功能](#扩展功能)
- [总结](#总结)

## 🏗️ 项目结构设置

首先创建类似的项目结构：

```
src/main/java/com/yourmod/
├── YourMod.java                    # 主模组类
├── AllTags.java                    # 标签管理
├── AllItems.java                   # 物品注册
├── AllCreativeModeTabs.java        # 创造模式标签页
└── datagen/
    └── YourModItemTagProvider.java # 数据生成器

src/main/resources/
├── assets/yourmod/
│   ├── models/item/
│   │   └── andesite_alloy.json    # 物品模型
│   ├── textures/item/
│   │   └── andesite_alloy.png     # 物品纹理
│   └── lang/
│       └── en_us.json             # 英文本地化
└── data/yourmod/
    ├── tags/items/
    │   └── create_ingots.json     # 物品标签
    └── recipes/
        └── andesite_alloy.json    # 配方文件
```

## 🔧 依赖配置

### build.gradle 配置

```gradle
plugins {
    id 'net.minecraftforge.gradle' version '5.1.+'
    id 'org.parchmentmc.librarian.forgegradle' version '1.+'
    id 'maven-publish'
}

version = '1.0.0'
group = 'com.yourmod'
archivesBaseName = 'yourmod'

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

minecraft {
    mappings channel: 'official', version: '1.20.1'
    
    runs {
        client {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'REGISTRIES'
            property 'forge.logging.console.level', 'debug'
            mods {
                yourmod {
                    source sourceSets.main
                }
            }
        }
        
        server {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'REGISTRIES'
            property 'forge.logging.console.level', 'debug'
            mods {
                yourmod {
                    source sourceSets.main
                }
            }
        }
        
        data {
            workingDirectory project.file('run')
            property 'forge.logging.markers', 'REGISTRIES'
            property 'forge.logging.console.level', 'debug'
            args '--mod', 'yourmod', '--all', '--output', file('src/generated/resources/'), '--existing', file('src/main/resources/')
            mods {
                yourmod {
                    source sourceSets.main
                }
            }
        }
    }
}

repositories {
    // Registrate 仓库
    maven {
        name = 'tterrag maven'
        url = 'https://maven.tterrag.com/'
    }
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.2.6'
    
    // Registrate 依赖
    implementation fg.deobf('com.tterrag.registrate:Registrate:MC1.20-1.3.3')
    
    // Parchment 映射
    mappings channel: 'official', version: '1.20.1'
}

sourceSets.main.resources { srcDir 'src/generated/resources' }
```

## ⚠️ 重要说明

### 版本兼容性
- **Minecraft**: 1.20.1
- **Forge**: 47.2.6+
- **Java**: 17+
- **Registrate**: MC1.20-1.3.3

### NeoForge 用户
如果你使用的是 NeoForge 而不是 Forge，请相应地调整：
- 将 `net.minecraftforge` 包名改为 `net.neoforged`
- 调整相应的依赖配置
- 使用 NeoForge 的 Gradle 插件

## 🔧 必需的配置文件

### mods.toml 文件

创建 `src/main/resources/META-INF/mods.toml`：

```toml
modLoader="javafml"
loaderVersion="[47,)"
license="MIT"
showAsResourcePack=false

[[mods]]
modId="yourmod"
version="${file.jarVersion}"
displayName="Your Mod"
description='''
A Create addon mod featuring andesite alloy and more!
'''
logoFile="icon.png"
credits=""
authors="Your Name"
displayURL="https://github.com/yourname/yourmod"
updateJSONURL=""

[[dependencies.yourmod]]
modId="forge"
mandatory=true
versionRange="[47.2.6,)"
ordering="NONE"
side="BOTH"

[[dependencies.yourmod]]
modId="minecraft"
mandatory=true
versionRange="[1.20.1,1.21)"
ordering="NONE"
side="BOTH"
```

### pack.mcmeta 文件

创建 `src/main/resources/pack.mcmeta`：

```json
{
  "pack": {
    "description": "Your Mod Resources",
    "pack_format": 15,
    "forge:resource_pack_format": 15,
    "forge:data_pack_format": 12
  }
}
```

## 🏷️ 标签系统实现

### AllTags.java

```java
package com.yourmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collections;

public class AllTags {
    
    // 工具方法
    public static <T> TagKey<T> optionalTag(IForgeRegistry<T> registry, ResourceLocation id) {
        return registry.tags().createOptionalTagKey(id, Collections.emptySet());
    }

    public static <T> TagKey<T> forgeTag(IForgeRegistry<T> registry, String path) {
        return optionalTag(registry, new ResourceLocation("forge", path));
    }

    public static TagKey<Item> forgeItemTag(String path) {
        return forgeTag(ForgeRegistries.ITEMS, path);
    }

    // 命名空间枚举
    public enum NameSpace {
        MOD(YourMod.MODID, false, true),
        FORGE("forge");

        public final String id;
        public final boolean optionalDefault;
        public final boolean alwaysDatagenDefault;

        NameSpace(String id) {
            this(id, true, false);
        }

        NameSpace(String id, boolean optionalDefault, boolean alwaysDatagenDefault) {
            this.id = id;
            this.optionalDefault = optionalDefault;
            this.alwaysDatagenDefault = alwaysDatagenDefault;
        }
    }

    // 物品标签
    public enum AllItemTags {
        CREATE_INGOTS;

        public final TagKey<Item> tag;
        public final boolean alwaysDatagen;

        AllItemTags() {
            this(NameSpace.MOD);
        }

        AllItemTags(NameSpace namespace) {
            this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        AllItemTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
            ResourceLocation id = new ResourceLocation(namespace.id, name().toLowerCase());
            if (optional) {
                tag = optionalTag(ForgeRegistries.ITEMS, id);
            } else {
                tag = ItemTags.create(id);
            }
            this.alwaysDatagen = alwaysDatagen;
        }

        public boolean matches(Item item) {
            return item.builtInRegistryHolder().is(tag);
        }

        public boolean matches(ItemStack stack) {
            return stack.is(tag);
        }
    }

    public static void init() {
        // 触发枚举初始化
    }
}
```

## 📦 物品注册实现

### YourMod.java (主模组类)

```java
package com.yourmod;

import com.tterrag.registrate.Registrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(YourMod.MODID)
public class YourMod {
    public static final String MODID = "yourmod";
    
    private static Registrate REGISTRATE;

    public YourMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // 初始化 Registrate
        REGISTRATE = Registrate.create(MODID);
        REGISTRATE.registerEventListeners(modEventBus);
        
        // 注册内容
        AllTags.init();
        AllItems.register();
        
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static Registrate registrate() {
        return REGISTRATE;
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
```

### AllCreativeModeTabs.java

```java
package com.yourmod;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class AllCreativeModeTabs {
    
    public static final RegistryEntry<CreativeModeTab> BASE_CREATIVE_TAB = 
        YourMod.registrate()
            .object("base")
            .simple(Registries.CREATIVE_MODE_TAB, () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.yourmod.base"))
                .icon(() -> new ItemStack(AllItems.ANDESITE_ALLOY.get()))
                .displayItems((params, output) -> {
                    output.accept(AllItems.ANDESITE_ALLOY.get());
                })
                .build());

    public static void register() {}
}
```

### AllItems.java

```java
package com.yourmod;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class AllItems {
    private static final Registrate REGISTRATE = YourMod.registrate();

    static {
        REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
    }

    // 核心物品 - ANDESITE_ALLOY
    public static final ItemEntry<Item> ANDESITE_ALLOY = 
        taggedIngredient("andesite_alloy", AllTags.AllItemTags.CREATE_INGOTS.tag);

    // 工具方法
    @SafeVarargs
    private static ItemEntry<Item> taggedIngredient(String name, TagKey<Item>... tags) {
        return REGISTRATE.item(name, Item::new)
            .tag(tags)
            .register();
    }

    private static ItemEntry<Item> ingredient(String name) {
        return REGISTRATE.item(name, Item::new)
            .register();
    }

    public static void register() {
        // 触发静态初始化
    }
}
```

## 📊 数据生成

### 数据生成器类

```java
package com.yourmod.datagen;

import com.yourmod.AllItems;
import com.yourmod.AllTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class YourModItemTagProvider extends ItemTagsProvider {
    
    public YourModItemTagProvider(PackOutput output, 
                                  CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  CompletableFuture<TagLookup<Block>> blockTags,
                                  ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, "yourmod", existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 添加 ANDESITE_ALLOY 到 CREATE_INGOTS 标签
        tag(AllTags.AllItemTags.CREATE_INGOTS.tag)
            .add(AllItems.ANDESITE_ALLOY.get());
    }
}
```

### 主数据生成类

```java
package com.yourmod.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = "yourmod", bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // 添加数据生成器
        generator.addProvider(event.includeServer(), 
            new YourModItemTagProvider(packOutput, lookupProvider, 
                CompletableFuture.supplyAsync(() -> null), existingFileHelper));
    }
}
```

## 🍳 配方系统

### data/yourmod/recipes/andesite_alloy.json

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "SAS",
    "AIA",
    "SAS"
  ],
  "key": {
    "S": {
      "item": "minecraft:andesite"
    },
    "A": {
      "item": "minecraft:zinc_ingot"
    },
    "I": {
      "item": "minecraft:iron_nugget"
    }
  },
  "result": {
    "item": "yourmod:andesite_alloy",
    "count": 2
  }
}
```

### Create 模组风格的配方（可选）

如果你想要更符合 Create 模组风格的配方：

```json
{
  "type": "create:mixing",
  "ingredients": [
    {
      "item": "minecraft:andesite"
    },
    {
      "item": "minecraft:iron_nugget"
    }
  ],
  "results": [
    {
      "item": "yourmod:andesite_alloy",
      "count": 1
    }
  ],
  "heatRequirement": "heated"
}
```

## 🌐 本地化文件

### assets/yourmod/lang/en_us.json

```json
{
  "item.yourmod.andesite_alloy": "Andesite Alloy",
  "itemGroup.yourmod.base": "Your Mod",
  "tag.item.yourmod.create_ingots": "Create Ingots"
}
```

### assets/yourmod/lang/zh_cn.json

```json
{
  "item.yourmod.andesite_alloy": "安山合金",
  "itemGroup.yourmod.base": "你的模组",
  "tag.item.yourmod.create_ingots": "机械动力锭"
}
```

## 🎨 纹理资源

### assets/yourmod/models/item/andesite_alloy.json

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "yourmod:item/andesite_alloy"
  }
}
```

### 纹理文件
- 创建 `assets/yourmod/textures/item/andesite_alloy.png` (16x16 像素)
- 可以从 Create 模组中复制相应的纹理文件作为参考

## 🏷️ 标签数据文件

### data/yourmod/tags/items/create_ingots.json

```json
{
  "values": [
    "yourmod:andesite_alloy"
  ]
}
```

## ✅ 测试验证

### 测试清单

1. **编译测试**
   ```bash
   ./gradlew build
   ```

2. **游戏内测试**
   - 启动开发环境
   - 检查创造模式物品栏
   - 验证物品显示正确
   - 测试配方制作

3. **标签验证**
   ```java
   // 在游戏中验证标签
   ItemStack stack = new ItemStack(AllItems.ANDESITE_ALLOY.get());
   boolean hasTag = AllTags.AllItemTags.CREATE_INGOTS.matches(stack);
   ```

4. **数据生成验证**
   ```bash
   ./gradlew runData
   ```

## 🔄 常见问题解决

### 问题1: ItemEntry 无法解析
**解决方案**: 确保正确添加了 Registrate 依赖

### 问题2: 标签不生效
**解决方案**: 检查数据生成是否正确运行，确保 JSON 文件格式正确

### 问题3: 纹理不显示
**解决方案**: 检查纹理路径和模型文件是否正确

### 问题5: 创造模式标签页不显示
**解决方案**: 检查 `AllCreativeModeTabs.register()` 是否被调用

### 问题6: 数据生成失败
**解决方案**: 确保数据生成器类正确注册到事件总线

### 问题7: mods.toml 配置错误
**解决方案**: 检查版本范围和依赖配置是否正确

## 📚 扩展功能

完成基础实现后，你可以添加：

1. **更多Create风格物品**
   - 黄铜锭 (Brass Ingot)
   - 锌锭 (Zinc Ingot)
   - 更多合金类型

2. **自定义配方类型**
   - 压块机配方
   - 洗涤配方
   - 混合配方

3. **JEI/REI 集成**
   - 配方显示
   - 物品信息

4. **Create 模组集成**
   - 直接使用 Create 的机器
   - 兼容 Create 的配方类型

5. **Patchouli 文档**
   - 游戏内手册
   - 配方指南

6. **与其他模组的兼容性**
   - Tinkers' Construct
   - Thermal Expansion
   - Immersive Engineering

## 🎯 总结

通过以上步骤，你应该能够完全复制 Create 模组中 `ANDESITE_ALLOY` 的所有功能。关键要点：

- 使用 Registrate 进行注册管理
- 正确设置标签系统
- 实现数据生成
- 添加合适的纹理和本地化

记住在每个步骤后进行测试，确保功能正常工作。
