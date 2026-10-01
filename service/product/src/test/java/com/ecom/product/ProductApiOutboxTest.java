package com.ecom.product;

import com.ecom.outbox.entity.OutboxEntity;
import com.ecom.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Goes through the real controllers, security and database: HTTP in, outbox rows out
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:productapi", "outbox.relay.enabled=false"})
@AutoConfigureMockMvc
class ProductApiOutboxTest {

    @Autowired MockMvc mvc;
    @Autowired OutboxRepository outboxRepository;
    @Autowired ObjectMapper mapper;

    private final UUID sellerId = UUID.randomUUID();
    // The simulated token format is "<user id>:<ROLE>"
    private final String bearer = "Bearer " + sellerId + ":SELLER";

    @BeforeEach
    void clean() {
        outboxRepository.deleteAll();
    }

    // Returns the "Aggregate:Action" of every recorded event, oldest first
    private List<String> actions() throws Exception {
        List<OutboxEntity> rows = new ArrayList<>(outboxRepository.findAll());
        rows.sort(Comparator.comparing(OutboxEntity::getCreatedAt));
        List<String> out = new ArrayList<>();
        for (OutboxEntity row : rows) {
            JsonNode json = mapper.readTree(row.getPayload());
            out.add(json.get("aggregate").asText() + ":" + json.get("action").asText());
        }
        return out;
    }

    @Test
    void theWholeLifecycleOverHttpRecordsOneEventPerStateChange() throws Exception {
        // Create a product
        MvcResult created = mvc.perform(post("/products").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Phone\",\"description\":\"desc\",\"price\":10.5}"))
                .andExpect(status().isCreated()).andReturn();
        String productId = mapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        // Update it
        mvc.perform(put("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Phone 2\",\"description\":\"desc\",\"price\":11.5}"))
                .andExpect(status().isOk());

        // Add a variant
        MvcResult added = mvc.perform(post("/products/" + productId + "/variants").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"properties\":{\"color\":\"red\"}}"))
                .andExpect(status().isCreated()).andReturn();
        long variantId = mapper.readTree(added.getResponse().getContentAsString()).get("id").asLong();

        // Remove the variant, still answered with an empty 204
        mvc.perform(delete("/products/" + productId + "/variants/" + variantId).header("Authorization", bearer))
                .andExpect(status().isNoContent());

        // Delete the product, still answered with an empty 204
        mvc.perform(delete("/products/" + productId).header("Authorization", bearer))
                .andExpect(status().isNoContent());

        // One event per state change, in order
        assertEquals(List.of("Product:CREATE", "Product:UPDATE", "Variant:ADD", "Variant:REMOVE", "Product:DELETE"),
                actions());
    }

    @Test
    void aRequestThatFailsRecordsNoEvent() throws Exception {
        // Update a product that does not exist
        mvc.perform(put("/products/" + UUID.randomUUID()).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Phone\",\"description\":\"d\",\"price\":1.0}"))
                .andExpect(status().isNotFound());

        // The failure left the outbox empty
        assertEquals(List.of(), actions());
    }
}
