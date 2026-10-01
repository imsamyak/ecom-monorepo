package com.ecom.product.domain.converter;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariantSkuConverterTest {

    private final VariantSkuConverter converter = new VariantSkuConverter();

    // Builds a sorted property map from key/value pairs
    private static TreeMap<String, String> props(String... kv) {
        TreeMap<String, String> map = new TreeMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }

    @Test
    void writesPropertiesAsKeyColonValuePairsJoinedByCommas() {
        // Two properties become one comma separated string
        String sku = converter.convertToDatabaseColumn(props("color", "red", "size", "M"));

        // Each pair is key:value
        assertEquals("color:red,size:M", sku);
    }

    @Test
    void writesPairsInSortedKeyOrderWhateverTheInsertionOrder() {
        // Insert in reverse alphabetical order
        String sku = converter.convertToDatabaseColumn(props("size", "M", "color", "red"));

        // Output is still sorted by key, so the same variant always has the same sku
        assertEquals("color:red,size:M", sku);
    }

    @Test
    void writesASinglePropertyWithoutTrailingComma() {
        // One property
        String sku = converter.convertToDatabaseColumn(props("color", "red"));

        // No separator is added
        assertEquals("color:red", sku);
    }

    @Test
    void stripsSurroundingWhitespaceFromKeysAndValues() {
        // Padded key and value
        String sku = converter.convertToDatabaseColumn(props("  color ", " red  "));

        // Padding is removed
        assertEquals("color:red", sku);
    }

    @Test
    void dropsEntriesWhoseKeyOrValueIsBlank() {
        // One good entry, one blank key, one blank value
        String sku = converter.convertToDatabaseColumn(props("color", "red", " ", "x", "size", " "));

        // Only the good entry survives
        assertEquals("color:red", sku);
    }

    @Test
    void writesEmptyStringOnlyWhenThereIsNothingToStore() {
        // No properties at all, and only blank ones
        assertEquals("", converter.convertToDatabaseColumn(props()));
        assertEquals("", converter.convertToDatabaseColumn(props(" ", " ")));
    }

    @Test
    void readsKeyColonValuePairsBackIntoASortedMap() {
        // Parse a stored sku
        TreeMap<String, String> properties = converter.convertToEntityAttribute("color:red,size:M");

        // Both pairs are restored
        assertEquals(Map.of("color", "red", "size", "M"), properties);
    }

    @Test
    void readsASinglePair() {
        // Parse a one-pair sku
        assertEquals(Map.of("color", "red"), converter.convertToEntityAttribute("color:red"));
    }

    @Test
    void readsEmptyStringAsAnEmptyMapInsteadOfFailing() {
        // An empty column value must not crash the read
        assertTrue(converter.convertToEntityAttribute("").isEmpty());
    }

    @Test
    void roundTripKeepsTheProperties() {
        // Write then read
        TreeMap<String, String> original = props("color", "red", "size", "M", "material", "cotton");
        TreeMap<String, String> restored = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original));

        // Nothing is lost or changed
        assertEquals(original, restored);
    }
}
