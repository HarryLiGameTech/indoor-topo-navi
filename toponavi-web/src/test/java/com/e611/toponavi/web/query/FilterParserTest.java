package com.e611.toponavi.web.query;

import com.e611.toponavi.web.api.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class FilterParserTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final FilterParser parser = new FilterParser(16, 256, 1024, 16384, 256);

    private boolean matches(String predicate, String record) throws Exception {
        return parser.parse(mapper.readTree(predicate), Set.of("attributes")).test(mapper.readTree(record));
    }

    @Test
    void distinguishesMissingNullAndNegation() throws Exception {
        assertFalse(matches("{\"field\":\"attributes.capacity\",\"op\":\"ne\",\"value\":null}", "{\"attributes\":{}}"));
        assertFalse(matches("{\"field\":\"attributes.capacity\",\"op\":\"isNotNull\"}", "{\"attributes\":{\"capacity\":null}}"));
        assertTrue(matches("{\"field\":\"attributes.capacity\",\"op\":\"exists\"}", "{\"attributes\":{\"capacity\":null}}"));
        assertTrue(matches("{\"not\":{\"field\":\"attributes.tags\",\"op\":\"contains\",\"value\":\"outdoor\"}}", "{}"));
    }

    @Test
    void comparesNumbersExactlyAndDoesNotCoerceStringsOrBooleans() throws Exception {
        assertTrue(matches("{\"field\":\"attributes.capacity\",\"op\":\"eq\",\"value\":12.0}", "{\"attributes\":{\"capacity\":12}}"));
        assertFalse(matches("{\"field\":\"attributes.capacity\",\"op\":\"gte\",\"value\":12}", "{\"attributes\":{\"capacity\":\"14\"}}"));
        assertTrue(matches("{\"field\":\"attributes.capacity\",\"op\":\"gt\",\"value\":9007199254740992}", "{\"attributes\":{\"capacity\":9007199254740993}}"));
        assertFalse(matches("{\"field\":\"attributes.capacity\",\"op\":\"eq\",\"value\":1}", "{\"attributes\":{\"capacity\":true}}"));
    }

    @Test
    void composesMembershipAndCustomPathsWithStructuralEquality() throws Exception {
        assertTrue(matches("""
                {"and":[{"field":["attributes","vendor.capacity"],"op":"gte","value":12},
                  {"or":[{"field":"attributes.tags","op":"contains","value":"facility"},
                    {"field":"attributes.tags","op":"contains","value":"shop"}]}]}
                """, "{\"attributes\":{\"vendor.capacity\":20,\"tags\":[\"facility\"]}}"));
        assertTrue(matches("{\"field\":\"attributes.record\",\"op\":\"in\",\"value\":[{\"a\":1.0,\"b\":[true]}]}",
                "{\"attributes\":{\"record\":{\"b\":[true],\"a\":1}}}"));
        assertFalse(matches("{\"field\":\"attributes.tags\",\"op\":\"contains\",\"value\":\"facility\"}", "{\"attributes\":{\"tags\":[\"Facility\"]}}"));
        assertFalse(matches("{\"field\":\"attributes.tags\",\"op\":\"in\",\"value\":[]}", "{\"attributes\":{\"tags\":[]}}"));
    }

    @Test
    void validatesEveryBranchAndBoundsStructure() throws Exception {
        for (String json : new String[]{"null", "{}", "{\"and\":[]}",
                "{\"field\":\"attributes\",\"op\":\"eval\",\"value\":\"x\"}",
                "{\"field\":\"attributes\",\"op\":\"exists\",\"value\":null}",
                "{\"field\":\"params.x\",\"op\":\"exists\"}",
                "{\"field\":\"attributes..x\",\"op\":\"exists\"}",
                "{\"or\":[{\"field\":\"attributes\",\"op\":\"exists\"},{}]}"}) {
            var parsed = mapper.readTree(json);
            assertThrows(ApiException.class, () -> parser.parse(parsed, Set.of("attributes")));
        }
        var filter = mapper.readTree("{\"not\":{\"field\":\"attributes\",\"op\":\"exists\"}}");
        assertTrue(assertThrows(ApiException.class, () -> new FilterParser(1, 1, 1, 1, 1)
                .parse(filter, Set.of("attributes"))).body().toString().contains("FILTER_TOO_COMPLEX"));
    }
}
