package org.dimdev.vanillafix.client.modsupport;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.FloatListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import org.dimdev.vanillafix.VanillaFix;
import org.dimdev.vanillafix.util.annotation.Exclude;

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
                    .setSavingRunnable(VanillaFix::save)
                    .setTitle(new TranslatableText("vanillafix.config.title"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            try {
                for (Field field : VanillaFix.config().getClass().getDeclaredFields()) {
                    int mods = field.getModifiers();
                    if (Modifier.isStatic(mods) || Modifier.isFinal(mods) || Modifier.isTransient(mods) || field.isAnnotationPresent(Exclude.class)) {
                        continue;
                    }
                    String name = field.getName();
                    ConfigCategory cat = builder.getOrCreateCategory(new TranslatableText("vanillafix.config.category." + name));
                    Object value = field.get(VanillaFix.config());
                    for (Field innerField: value.getClass().getDeclaredFields()) {
                        int innerMods = innerField.getModifiers();
                        if (Modifier.isStatic(innerMods) || Modifier.isFinal(innerMods) || Modifier.isTransient(innerMods)) {
                            continue;
                        }
                        String innerName = innerField.getName();
                        Class<?> innerType = innerField.getType();
                        Text text = new TranslatableText("vanillafix.config.value." + name + "." + innerName);
                        if (innerType == boolean.class || innerType == Boolean.class) {
                            BooleanListEntry entry = entryBuilder.startBooleanToggle(text, innerField.getBoolean(value))
                                    .requireRestart()
                                    .setSaveConsumer(bl -> {
                                        try {
                                            innerField.setBoolean(value, bl);
                                        } catch (IllegalAccessException e) {
                                            throw new AssertionError();
                                        }
                                    })
                                    .build();
                            cat.addEntry(entry);
                        } else if (innerType == int.class || innerType == Integer.class) {
                            IntegerListEntry entry = entryBuilder.startIntField(text, innerField.getInt(value))
                                    .requireRestart()
                                    .setSaveConsumer(i -> {
                                        try {
                                            innerField.setInt(value, i);
                                        } catch (IllegalAccessException e) {
                                            throw new AssertionError();
                                        }
                                    }).build();
                            cat.addEntry(entry);
                        } else if (innerType == float.class || innerType == Float.class) {
                            FloatListEntry entry = entryBuilder.startFloatField(text, innerField.getFloat(value))
                                    .requireRestart()
                                    .setSaveConsumer(f -> {
                                        try {
                                            innerField.setFloat(value, f);
                                        } catch (IllegalAccessException e) {
                                            throw new AssertionError();
                                        }
                                    }).build();
                            cat.addEntry(entry);
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
