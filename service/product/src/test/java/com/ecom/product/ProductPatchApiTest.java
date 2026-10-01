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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:patchapi", "outbox.relay.enabled=false"})
@AutoConfigureMockMvc
class ProductPatchApiTest {

    @Autowired MockMvc mvc;
    @Autowired OutboxRepository outboxRepository;
    @Autowired ObjectMapper mapper;

    private final UUID sellerId = UUID.randomUUID();
    private final String bearer = "Bearer " + sellerId + ":SELLER";
    private String productId;

    @BeforeEach
    void setup() throws Exception {
        outboxRepository.deleteAll();
        MvcResult created = mvc.perform(post("/products").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Phone\",\"description\":\"desc\",\"price\":10.5}"))
                .andExpect(status().isCreated()).andReturn();
        productId = mapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
        outboxRepository.deleteAll();
    }

    private void assertOutbox(String action, String expectedStatus) throws Exception {
        List<OutboxEntity> rows = new ArrayList<>(outboxRepository.findAll());
        assertEquals(1, rows.size(), "expected exactly 1 row");
        OutboxEntity row = rows.get(0);
        JsonNode json = mapper.readTree(row.getPayload());
        assertEquals("Product", json.get("aggregate").asText());
        assertEquals(action, json.get("action").asText());
        if (expectedStatus != null) {
            assertEquals(expectedStatus, json.get("data").get("status").asText());
        }
    }

    private void assertNoOutbox() {
        assertEquals(0, outboxRepository.count(), "expected no outbox rows");
    }

    @Test
    void patchingTitleAloneLeavesOtherFieldsUnchanged() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Phone 2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Phone 2"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.price").value(10.5))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        assertOutbox("UPDATE", "INACTIVE");
    }

    @Test
    void patchingDescriptionAloneLeavesOtherFieldsUnchanged() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"new desc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Phone"))
                .andExpect(jsonPath("$.description").value("new desc"))
                .andExpect(jsonPath("$.price").value(10.5));
        assertOutbox("UPDATE", "INACTIVE");
    }

    @Test
    void patchingPriceAloneLeavesOtherFieldsUnchanged() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":20.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(20.5));
        assertOutbox("UPDATE", "INACTIVE");
    }

    @Test
    void patchingStatusAloneLeavesOtherFieldsUnchanged() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        assertOutbox("UPDATE", "ACTIVE");
    }

    @Test
    void patchingSeveralFieldsChangesThemAndLeavesTheRestUnchanged() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Phone 3\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Phone 3"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.price").value(10.5));
        assertOutbox("UPDATE", "ACTIVE");
    }

    @Test
    void patchingDescriptionToNullClearsIt() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").doesNotExist());
        assertOutbox("UPDATE", "INACTIVE");
    }

    @Test
    void patchingTitleToNullGives400() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":null}"))
                .andExpect(status().isBadRequest());
        assertNoOutbox();
    }

    @Test
    void patchingPriceToNullGives400() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":null}"))
                .andExpect(status().isBadRequest());
        assertNoOutbox();
    }

    @Test
    void patchingStatusToNullGives400() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":null}"))
                .andExpect(status().isBadRequest());
        assertNoOutbox();
    }

    @Test
    void patchingAnUnknownStatusValueGives400() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"UNKNOWN_STATUS\"}"))
                .andExpect(status().isBadRequest());
        assertNoOutbox();
    }

    @Test
    void patchingTitleTooShortGives400() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"ab\"}"))
                .andExpect(status().isBadRequest());
        assertNoOutbox();
    }

    @Test
    void patchingTitleTooLongGives400() throws Exception {
        String longTitle = "a".repeat(101);
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + longTitle + "\"}"))
                .andExpect(status().isBadRequest());
        assertNoOutbox();
    }

    @Test
    void patchingDescriptionOver500Gives400() throws Exception {
        String longDesc = "d".repeat(501);
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"" + longDesc + "\"}"))
                .andExpect(status().isBadRequest());
        assertNoOutbox();
    }

    @Test
    void patchingPriceZeroGives400() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":0}"))
                .andExpect(status().isBadRequest());
        assertNoOutbox();
    }

    @Test
    void patchingPriceNegativeGives400() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":-1}"))
                .andExpect(status().isBadRequest());
        assertNoOutbox();
    }

    @Test
    void patchingWithAnEmptyBodyGives400() throws Exception {
        mvc.perform(patch("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        assertNoOutbox();
    }

    @Test
    void patchingAMissingProductGives404() throws Exception {
        mvc.perform(patch("/products/" + UUID.randomUUID()).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Phone 2\"}"))
                .andExpect(status().isNotFound());
        assertNoOutbox();
    }

    @Test
    void patchingAProductOwnedByAnotherSellerGives403() throws Exception {
        mvc.perform(patch("/products/" + productId)
                        .header("Authorization", "Bearer " + UUID.randomUUID() + ":SELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Phone 2\"}"))
                .andExpect(status().isForbidden());
        assertNoOutbox();
    }

    @Test
    void putOnProductNoLongerWorks() throws Exception {
        mvc.perform(put("/products/" + productId).header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Phone 2\",\"description\":\"d\",\"price\":1.0}"))
                .andExpect(result -> assertTrue(result.getResponse().getStatus() == 405 || result.getResponse().getStatus() == 404, 
                        "Actual status: " + result.getResponse().getStatus()));
    }

    @Test
    void patchActiveOnProductNoLongerWorks() throws Exception {
        mvc.perform(patch("/products/" + productId + "/active").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(result -> assertTrue(result.getResponse().getStatus() == 405 || result.getResponse().getStatus() == 404, 
                        "Actual status: " + result.getResponse().getStatus()));
    }
}
