package ske.registry.util;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public final class JsonHelper {

    private JsonHelper() {
    }

    public static String tilJson(Map<String, String> orderedMap) {
        return "{" + Joiner.on(", ").join(Iterables.transform(orderedMap.entrySet(), new Function<Map.Entry<String, String>, String>() {
            @Override
            public String apply(Map.Entry<String, String> input) {
                return String.format("\"%s\":\"%s\"", unescapeJava(input.getKey()), unescapeJava(input.getValue()));
            }
        })) + "}";
    }

    public static String tilJson(String k1, String v1) {
        return tilJson(ImmutableMap.of(k1, v1));
    }

    public static String tilJson(String k1, String v1, String k2, String v2) {
        return tilJson(ImmutableMap.of(
                k1, v1,
                k2, v2));
    }

    public static String tilJson(String k1, String v1, String k2, String v2, String k3, String v3) {
        return tilJson(ImmutableMap.of(
                k1, v1,
                k2, v2,
                k3, v3));
    }

    public static String tilJson(String k1, String v1, String k2, String v2, String k3, String v3, String k4, String v4) {
        return tilJson(ImmutableMap.of(
                k1, v1,
                k2, v2,
                k3, v3,
                k4, v4));
    }

    public static String tilJson(String k1, String v1, String k2, String v2, String k3, String v3, String k4, String v4, String k5, String v5) {
        return tilJson(ImmutableMap.of(
                k1, v1,
                k2, v2,
                k3, v3,
                k4, v4,
                k5, v5));
    }

    public static String tilJson(String k1, String v1, String k2, String v2, String k3, String v3,
                                 String k4, String v4, String k5, String v5, String k6, String v6) {
        return tilJson(ImmutableMap.<String, String>builder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .put(k4, v4)
                .put(k5, v5)
                .put(k6, v6)
                .build());
    }

}
