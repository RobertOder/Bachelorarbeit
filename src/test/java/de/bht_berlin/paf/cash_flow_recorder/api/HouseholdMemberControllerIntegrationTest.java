package de.bht_berlin.paf.cash_flow_recorder.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.bht_berlin.paf.cash_flow_recorder.entity.HouseholdMember;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-Tests for the householdMember-Controller
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HouseholdMemberControllerIntegrationTest {

    private static HouseholdMember householdMember;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void setUp() {
        Date testDate = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            testDate = dateFormat.parse("12-12-2024");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        householdMember = new HouseholdMember("Mustermann", "Max", "max@mustermann.de",
                "030123456", "Straße 12", "Stadt", "Bundesland", "12345", "Land",
                "StrengGeheim", testDate);
    }

    // At first testing only the CRUD-Operations

    /**
     * Unit-Test for the creation of an householdMember
     * @throws Exception
     */
    @Test
    @Order(1)
    void householdMemberCreation() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String householdMemberJsonString = objectMapper.writeValueAsString(householdMember);
        mockMvc.perform(patch("/householdMember/upsert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(householdMemberJsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mustermann"))
                .andExpect(jsonPath("$.firstname").value("Max"))
                .andExpect(jsonPath("$.email").value("max@mustermann.de"))
                .andExpect(jsonPath("$.phone").value("030123456"))
                .andExpect(jsonPath("$.address").value("Straße 12"))
                .andExpect(jsonPath("$.city").value("Stadt"))
                .andExpect(jsonPath("$.state").value("Bundesland"))
                .andExpect(jsonPath("$.zip").value("12345"))
                .andExpect(jsonPath("$.country").value("Land"))
                .andExpect(jsonPath("$.password").value("StrengGeheim"))
                .andExpect(jsonPath("$.created").value("2024-12-12T00:00:00.000+00:00"))
                .andExpect(result ->
                        assertTrue(result.getResponse().getContentAsString().contains("accounts"))
                )
                .andExpect(result ->
                        assertTrue(result.getResponse().getContentAsString().contains("households"))
                );

    }

    /**
     * Unit-Test for reading an householdMember (Controller returns a List)
     * @throws Exception
     */
    @Test
    @Order(2)
    void householdMemberRead() throws Exception {
        mockMvc.perform(get("/householdMember/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Mustermann"))
                .andExpect(jsonPath("$[0].firstname").value("Max"))
                .andExpect(jsonPath("$[0].email").value("max@mustermann.de"))
                .andExpect(jsonPath("$[0].phone").value("030123456"))
                .andExpect(jsonPath("$[0].address").value("Straße 12"))
                .andExpect(jsonPath("$[0].city").value("Stadt"))
                .andExpect(jsonPath("$[0].state").value("Bundesland"))
                .andExpect(jsonPath("$[0].zip").value("12345"))
                .andExpect(jsonPath("$[0].country").value("Land"))
                .andExpect(jsonPath("$[0].password").value("StrengGeheim"))
                .andExpect(jsonPath("$[0].created").value("2024-12-12T00:00:00.000+00:00"))
                .andExpect(result ->
                        assertTrue(result.getResponse().getContentAsString().contains("accounts"))
                )
                .andExpect(result ->
                        assertTrue(result.getResponse().getContentAsString().contains("households"))
                );
    }

    /**
     * Unit-Test for updating an householdMember
     * @throws Exception
     */
    @Test
    @Order(3)
    void householdMemberUpdate() throws Exception {
        HouseholdMember updatedHouseholdMember = new HouseholdMember("Mustermann", "Max", "max@mustermann.de",
                "030123456", "Straße 12", "Stadt", "Bundesland", "12345", "Land",
                "SuperGeheim", null);
        ObjectMapper objectMapper = new ObjectMapper();
        String householdMemberJsonString = objectMapper.writeValueAsString(updatedHouseholdMember);
        mockMvc.perform(patch("/householdMember/upsert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(householdMemberJsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value("SuperGeheim"));

    }

    /**
     * Unit-Test for deleting an householdMember (first removing and then try to read)
     * @throws Exception
     */
    @Test
    @Order(4)
    void householdMemberDeleteAndNotFound() throws Exception {
        mockMvc.perform(delete("/householdMember/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/householdMember/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());
    }
}
