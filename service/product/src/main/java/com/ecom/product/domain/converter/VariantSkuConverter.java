package com.ecom.product.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public @Converter class VariantSkuConverter implements AttributeConverter<TreeMap<String, String>, String> {

    @Override
    public String convertToDatabaseColumn(TreeMap<String, String> attribute) {
        // A missing map has nothing to store
        if (attribute == null) {
            return "";
        }
        // Turn each property into "key:value", trimming padding and dropping entries with a blank key or value
        // Join the pairs with commas; the map is sorted, so equal variants always produce the same sku
        return attribute.entrySet().stream().flatMap(e -> {
            String key = e.getKey().strip();
            String val = e.getValue().strip();
            if (key.isEmpty() || val.isEmpty()) {
                return Stream.empty();
            }
            return Stream.of(String.format("%s:%s", key, val));
        }).collect(Collectors.joining(","));
    }

    @Override
    public TreeMap<String, String> convertToEntityAttribute(String dbData) {
        TreeMap<String, String> properties = new TreeMap<>();
        // An empty or missing column means a variant without properties
        if (dbData == null || dbData.isBlank()) {
            return properties;
        }
        for (String attr : dbData.split(",")) {
            // Split at the first colon only, so a value may itself contain colons
            String[] pair = attr.split(":", 2);
            // Skip anything that is not a key:value pair
            if (pair.length < 2) {
                continue;
            }
            // Rebuild the property
            properties.put(pair[0], pair[1]);
        }
        return properties;
    }
}
