package utils

import com.google.gson.*
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import java.lang.reflect.Type

class DateTimeTypeAdapter : JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

    override fun deserialize(
        json: JsonElement, typeOfT: Type,
        context: JsonDeserializationContext
    ): DateTime = DateTime.parse(json.asString)

    override fun serialize(
        src: DateTime, typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement = JsonPrimitive(
        ISODateTimeFormat
            .dateTimeNoMillis()
            .print(src)
    )
}