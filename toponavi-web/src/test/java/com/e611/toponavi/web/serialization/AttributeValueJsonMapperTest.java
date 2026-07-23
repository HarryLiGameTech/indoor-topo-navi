package com.e611.toponavi.web.serialization;

import enums.AttributeValue;
import org.junit.jupiter.api.Test;
import scala.jdk.javaapi.CollectionConverters;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeValueJsonMapperTest {

    @Test
    void convertsGenericAndNestedListsRecursively() {
        AttributeValue value = AttributeValue.ListValue$.MODULE$.apply(scalaList(
                AttributeValue.StringValue$.MODULE$.apply("outdoor"),
                AttributeValue.IntValue$.MODULE$.apply(27),
                AttributeValue.ListValue$.MODULE$.apply(scalaList(
                        AttributeValue.DoubleValue$.MODULE$.apply(1.5),
                        AttributeValue.BoolValue$.MODULE$.apply(true)
                ))
        ));

        assertEquals(
                List.of("outdoor", 27, List.of(1.5, true)),
                AttributeValueJsonMapper.toJavaValue(value)
        );
    }

    private scala.collection.immutable.List<AttributeValue> scalaList(AttributeValue... values) {
        return CollectionConverters.asScala(Arrays.asList(values)).toList();
    }
}
