package org.dimdev.vanillafix.client.modsupport;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.FloatListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.dimdev.vanillafix.VanillaFix;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuSupport implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (parent) -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(new TranslatableText("vanillafix.config.title"));
            BiMap<String, ConfigCategory> categories = HashBiMap.create();
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            try {
                for (Field field : VanillaFix.config().getClass().getDeclaredFields()) {
                    int mods = field.getModifiers();
                    if (Modifier.isStatic(mods) || Modifier.isFinal(mods) || Modifier.isTransient(mods)) {
                        continue;
                    }
                    String name = field.getName();
                    ConfigCategory cat = builder.getOrCreateCategory(new TranslatableText("vanillafix.config.category." + name));
                    categories.put(name, cat);
                    Object value = field.get(VanillaFix.config());
                    for (Field innerField: value.getClass().getDeclaredFields()) {
                        int innerMods = innerField.getModifiers();
                        if (Modifier.isStatic(innerMods) || Modifier.isFinal(innerMods) || Modifier.isTransient(innerMods)) {
                            continue;
                        }
                        String innerName = field.getName();
                        Class<?> innerType = field.getType();
                        Text text = new TranslatableText("vanillafix.config.value." + name + "." + innerName);
                        if (innerType == boolean.class || innerType == Boolean.class) {
                            BooleanListEntry booleanListEntry = entryBuilder.startBooleanToggle(text, innerField.getBoolean(value))
                                    .requireRestart()
                                    .setSaveConsumer(bl -> {
                                        try {
                                            innerField.setBoolean(value, bl);
                                        } catch (IllegalAccessException e) {
                                            throw new AssertionError();
                                        }
                                    })
                                    .build();
                            cat.addEntry(booleanListEntry);
                        } else if (innerType == int.class || innerType == Integer.class) {
                            IntegerListEntry integerListEntry = entryBuilder.startIntField(text, innerField.getInt(value))
                                    .requireRestart()
                                    .setSaveConsumer(i -> {
                                        try {
                                            innerField.setInt(value, i);
                                        } catch (IllegalAccessException e) {
                                            throw new AssertionError();
                                        }
                                    }).build();
                            cat.addEntry(integerListEntry);
                        } else if (innerType == float.class || innerType == Float.class) {
                            FloatListEntry floatListEntry = entryBuilder.startFloatField(text, innerField.getFloat(value))
                                    .requireRestart()
                                    .setSaveConsumer(f -> {
                                        try {
                                            innerField.setFloat(value, f);
                                        } catch (IllegalAccessException e) {
                                            throw new AssertionError();
                                        }
                                    }).build();
                            cat.addEntry(floatListEntry);
                        }
                    }
                }
                return builder.build();
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            }
        };
    }
}
