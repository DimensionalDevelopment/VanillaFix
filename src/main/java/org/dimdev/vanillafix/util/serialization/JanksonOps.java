package org.dimdev.vanillafix.util.serialization;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

public enum JanksonOps implements DynamicOps<JsonElement> {
	INSTANCE;

	@Override
	public JsonElement empty() {
		return JsonNull.INSTANCE;
	}

	@Override
	public JsonElement createString(String value) {
		return new JsonPrimitive(value);
	}

	@Override
	public JsonElement createNumeric(Number i) {
		return new JsonPrimitive(i);
	}

	@Override
	public JsonElement createBoolean(boolean value) {
		return new JsonPrimitive(value);
	}

	@Override
	public <U> U convertTo(DynamicOps<U> outOps, JsonElement input) {
		if (input instanceof JsonObject) {
			return this.convertMap(outOps, input);
		} else if (input instanceof JsonArray) {
			return this.convertList(outOps, input);
		} else if (input instanceof JsonNull) {
			return outOps.empty();
		}
		JsonPrimitive inputPrimitive = (JsonPrimitive) input;
		if (inputPrimitive.getValue() instanceof String) {
			return outOps.createString(String.valueOf(inputPrimitive.getValue()));
		} else if (inputPrimitive.getValue() instanceof Boolean) {
			return outOps.createBoolean((Boolean) inputPrimitive.getValue());
		}
		BigDecimal numberExact = inputPrimitive.getValue() instanceof BigDecimal ? (BigDecimal) inputPrimitive.getValue() : new BigDecimal(inputPrimitive.getValue().toString());
		try {
			long l = numberExact.longValueExact();
			if ((byte) l == l) {
				return outOps.createByte((byte) l);
			}
			if ((short) l == l) {
				return outOps.createShort((short) l);
			}
			if ((int) l == l) {
				return outOps.createInt((int) l);
			}
			return outOps.createLong(l);
		} catch (ArithmeticException e) {
			double d = numberExact.doubleValue();
			if ((float) d == d) {
				return outOps.createFloat((float) d);
			}
			return outOps.createDouble(d);
		}
	}

	@Override
	public DataResult<Number> getNumberValue(JsonElement input) {
		if (input instanceof JsonPrimitive) {
			if (((JsonPrimitive) input).getValue() instanceof Number) {
				return DataResult.success((Number) ((JsonPrimitive) input).getValue());
			} else if (((JsonPrimitive) input).getValue() instanceof Boolean) {
				return DataResult.success(((Boolean) ((JsonPrimitive) input).getValue()) ? (byte) 1 : (byte) 0);
			}
		}
		return DataResult.error("Not a number: " + input);
	}

	@Override
	public DataResult<Boolean> getBooleanValue(JsonElement input) {
		if (input instanceof JsonPrimitive) {
			if (((JsonPrimitive) input).getValue() instanceof Boolean) {
				return DataResult.success((Boolean) ((JsonPrimitive) input).getValue());
			} else if (((JsonPrimitive) input).getValue() instanceof Number) {
				return DynamicOps.super.getBooleanValue(input);
			}
		}
		return DataResult.error("Not a boolean: " + input);
	}

	@Override
	public DataResult<String> getStringValue(JsonElement input) {
		if (input instanceof JsonPrimitive && ((JsonPrimitive) input).getValue() instanceof String) {
			return DataResult.success(String.valueOf(((JsonPrimitive) input).getValue()));
		}
		return DataResult.error("Not a string: " + input);
	}

	@Override
	public DataResult<JsonElement> mergeToList(JsonElement list, JsonElement value) {
		if (!(list instanceof JsonArray) && list != this.empty()) {
			return DataResult.error("mergeToList not called with a list: " + list, list);
		}

		JsonArray array = new JsonArray();
		if (list != this.empty()) {
			//noinspection ConstantConditions
			array.addAll((JsonArray) list);
		}
		array.add(value);
		return DataResult.success(array);
	}

	@Override
	public DataResult<JsonElement> mergeToMap(JsonElement map, JsonElement key, JsonElement value) {
		if (!(map instanceof JsonObject) && map != this.empty()) {
			return DataResult.error("mergeToMap not called with a map: " + map, map);
		}
		if (!(key instanceof JsonPrimitive) || !(((JsonPrimitive) key).getValue() instanceof String)) {
			return DataResult.error("Key is not a string: " + key, map);
		}

		JsonObject output = new JsonObject();
		if (map != this.empty()) {
			//noinspection ConstantConditions
			((JsonObject) map).forEach(output::put);
		}
		output.put(String.valueOf(((JsonPrimitive) key).getValue()), value);

		return DataResult.success(output);
	}

	@Override
	public DataResult<JsonElement> mergeToMap(JsonElement map, MapLike<JsonElement> values) {
		if (!(map instanceof JsonObject) && map != this.empty()) {
			return DataResult.error("mergeToMap not called with a map: " + map, map);
		}

		JsonObject newMap = new JsonObject();
		if (map != this.empty()) {
			//noinspection ConstantConditions
			((JsonObject) map).forEach(newMap::put);
		}

		List<JsonElement> missed = Lists.newArrayList();

		values.entries().forEach(entry -> {
			JsonElement key = entry.getFirst();
			//noinspection ConstantConditions
			if (!(key instanceof JsonPrimitive) || !(((JsonPrimitive) map).getValue() instanceof String)) {
				missed.add(key);
				return;
			}
			newMap.put(String.valueOf(((JsonPrimitive) key).getValue()), entry.getSecond());
		});

		if (!missed.isEmpty()) {
			return DataResult.error("Keys are not strings: " + missed, newMap);
		}

		return DataResult.success(newMap);
	}

	@Override
	public DataResult<Stream<Pair<JsonElement, JsonElement>>> getMapValues(JsonElement input) {
		if (!(input instanceof JsonObject)) {
			return DataResult.error("Not a json object: " + input);
		}
		return DataResult.success((((JsonObject) input).entrySet().stream().map(entry -> Pair.of(new JsonPrimitive(entry.getKey()), entry.getValue() instanceof JsonNull ? null : entry.getValue()))));
	}

	@Override
	public JsonElement createMap(Stream<Pair<JsonElement, JsonElement>> pairStream) {
		JsonObject jsonObject = new JsonObject();
		pairStream.forEach(p -> jsonObject.put(p.getFirst().toJson(), p.getSecond()));
		return jsonObject;
	}

	@Override
	public DataResult<Stream<JsonElement>> getStream(JsonElement input) {
		if (input instanceof JsonArray) {
			return DataResult.success(((JsonArray) input).stream().map(e -> e instanceof JsonNull ? null : e));
		}
		return DataResult.error("Not a json array: " + input);
	}

	@Override
	public JsonElement createList(Stream<JsonElement> input) {
		JsonArray array = new JsonArray();
		input.forEach(array::add);
		return array;
	}

	@Override
	public JsonElement remove(JsonElement input, String key) {
		if (input instanceof JsonObject) {
			JsonObject result = new JsonObject();
			((JsonObject) input).entrySet().stream().filter(entry -> !Objects.equals(entry.getKey(), key)).forEach(entry -> result.put(entry.getKey(), entry.getValue()));
			return result;
		}
		return input;
	}
}
