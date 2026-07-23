package com.e611.toponavi.web.serialization;

import enums.AttributeValue;
import scala.jdk.javaapi.CollectionConverters;

/** Converts compiler attribute values into Jackson-friendly Java values. */
public final class AttributeValueJsonMapper {

    private AttributeValueJsonMapper() {}

    public static Object toJavaValue(AttributeValue value) {
        if (value instanceof AttributeValue.IntValue v) return v.value();
        if (value instanceof AttributeValue.StringValue v) return v.value();
        if (value instanceof AttributeValue.BoolValue v) return v.value();
        if (value instanceof AttributeValue.DoubleValue v) return v.value();
        if (value instanceof AttributeValue.ListValue v) {
            return CollectionConverters.asJava(v.values()).stream()
                    .map(AttributeValueJsonMapper::toJavaValue)
                    .toList();
        }
        throw new IllegalArgumentException("Unsupported attribute value: " + value);
    }
}
