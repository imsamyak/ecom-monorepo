package com.ecom.contract;

import com.ecom.contract.event.ProductEvent;
import com.ecom.contract.event.VariantEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EventCatalog {

    // Cache the resolved event record classes by aggregate and action
    private static final Map<String, Class<? extends DomainEvent>> CATALOG = new HashMap<>();

    static {
        // List the sealed event interfaces
        List<Class<?>> interfaces = List.of(ProductEvent.class, VariantEvent.class);
        
        for (Class<?> iface : interfaces) {
            // The aggregate type is the interface name without the Event suffix
            String aggregate = iface.getSimpleName();
            if (aggregate.endsWith("Event")) {
                aggregate = aggregate.substring(0, aggregate.length() - 5);
            }
            
            // Map each permitted action (the nested records)
            for (Class<?> record : iface.getPermittedSubclasses()) {
                String action = record.getSimpleName();
                String key = aggregate + "/" + action;
                @SuppressWarnings("unchecked")
                Class<? extends DomainEvent> eventClass = (Class<? extends DomainEvent>) record;
                CATALOG.put(key, eventClass);
            }
        }
    }

    public static Optional<Class<? extends DomainEvent>> resolve(String aggregate, String action) {
        // Look up the record class by the combination of aggregate and action
        return Optional.ofNullable(CATALOG.get(aggregate + "/" + action));
    }
}
