package com.ecom.product.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public @Converter class VariantSkuConverter implements AttributeConverter<TreeMap<String, String>, String> {

    @Override
    public String convertToDatabaseColumn(TreeMap<String, String> attribute) {

        return String.join(attribute.entrySet().stream().flatMap(e -> {
            String key = e.getKey().strip();
            String val = e.getValue().strip();

            if (key.isEmpty() || val.isEmpty()) {
                return Stream.empty();
            }

            return Stream.of(String.format("%s:%s", key, val));
        }).collect(Collectors.joining(",")));

    }

    @Override
    public TreeMap<String, String> convertToEntityAttribute(String dbData) {

        return Arrays.stream(dbData.split(",")).collect(TreeMap::new, (map, attr) -> {
            String[] pair = attr.split(":");
            String key = pair[0];
            String val = pair[1];
            map.put(key, val);
        }, TreeMap::putAll);
    }
}
