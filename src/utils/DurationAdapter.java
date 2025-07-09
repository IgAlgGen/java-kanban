package utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

public class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
    @Override
    public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
        // Преобразуем в ISO-8601 строку, например "PT15M" или "PT1H30M"
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // Разбираем обратно из той же строки
        return Duration.parse(json.getAsString());
    }
}