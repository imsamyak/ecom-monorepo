package com.ecom.inbox.port.spi;

public interface InboxStore {
    
    /**
     * Checks if the event is a duplicate. Locks the row if it exists.
     * 
     * @param eventId The id of the event
     * @param aggregateType The type of the aggregate
     * @param aggregateId The id of the aggregate
     * @return true if the event has already been processed
     */
    boolean isDuplicate(String eventId, String aggregateType, String aggregateId);

    /**
     * Saves or overwrites the deduplication row for the aggregate with this new eventId and current time.
     * 
     * @param eventId The id of the event
     * @param aggregateType The type of the aggregate
     * @param aggregateId The id of the aggregate
     */
    void saveEvent(String eventId, String aggregateType, String aggregateId);
}
