package org.dimdev.vanillafix.util;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ConfigPair implements Cloneable {
	public static final Codec<ConfigPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("category").forGetter(ConfigPair::getCategory),
			Codec.STRING.fieldOf("value").forGetter(ConfigPair::getValue)
	).apply(instance, ConfigPair::new));
	private final String category;
	private final String value;

	public ConfigPair(String category, String value) {
		this.category = category;
		this.value = value;
	}

	public String getCategory() {
		return this.category;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConfigPair)) return false;
		ConfigPair that = (ConfigPair) o;
		return Objects.equals(this.category, that.category) &&
				Objects.equals(this.value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.category, this.value);
	}

	@Override
	public String toString() {
		return "ConfigPair{" +
				"category='" + this.category + '\'' +
				", value='" + this.value + '\'' +
				'}';
	}

	@Override
	public ConfigPair clone() {
		try {
			return (ConfigPair) super.clone();
		} catch (CloneNotSupportedException e) {
			// cant happen
			throw new AssertionError();
		}
	}
}
