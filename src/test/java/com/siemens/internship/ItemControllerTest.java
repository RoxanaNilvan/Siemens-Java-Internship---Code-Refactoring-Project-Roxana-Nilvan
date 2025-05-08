package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    // Test retrieving all items
    @Test
    void testGetAllItems() throws Exception {
        Item item1 = new Item(); item1.setId(101L); item1.setName("Test1");
        Item item2 = new Item(); item2.setId(102L); item2.setName("Test2");

        when(itemService.findAll()).thenReturn(Arrays.asList(item1, item2));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // Test retrieving one item by ID when it exists
    @Test
    void testGetItemByIdFound() throws Exception {
        Item item = new Item();
        item.setId(103L);
        item.setName("Found");

        when(itemService.findById(103L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/103"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Found"));
    }

    // Test retrieving an item by ID when it does not exist
    @Test
    void testGetItemByIdNotFound() throws Exception {
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/99"))
                .andExpect(status().isNotFound());
    }

    // Test creating a valid item
    @Test
    void testCreateItemValid() throws Exception {
        Item item = new Item();
        item.setName("Valid Item");
        item.setStatus("NEW");
        item.setDescription("Some desc");
        item.setEmail("test@example.com");

        when(itemService.save(any(Item.class))).thenReturn(item);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Valid Item"));
    }

    // Test creating an item with an invalid email
    @Test
    void testCreateItemInvalidEmail() throws Exception {
        Item item = new Item();
        item.setName("Invalid Email");
        item.setStatus("NEW");
        item.setDescription("Some desc");
        item.setEmail("not-an-email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    // Test updating an existing item
    @Test
    void testUpdateItemFound() throws Exception {
        Item item = new Item();
        item.setId(101L);
        item.setName("Updated");
        item.setStatus("NEW");
        item.setDescription("Some desc");
        item.setEmail("email@domain.com");

        when(itemService.findById(101L)).thenReturn(Optional.of(item));
        when(itemService.save(any(Item.class))).thenReturn(item);

        mockMvc.perform(put("/api/items/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    // Test updating a non-existent item
    @Test
    void testUpdateItemNotFound() throws Exception {
        Item item = new Item();
        item.setId(101L);
        item.setName("Doesn't matter");
        item.setStatus("NEW");
        item.setDescription("Some desc");
        item.setEmail("email@domain.com");

        when(itemService.findById(101L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound());
    }

    // Test deleting an item that exists
    @Test
    void testDeleteItemFound() throws Exception {
        Item item = new Item();
        item.setId(101L);
        when(itemService.findById(101L)).thenReturn(Optional.of(item));
        Mockito.doNothing().when(itemService).deleteById(101L);

        mockMvc.perform(delete("/api/items/101"))
                .andExpect(status().isNoContent());
    }

    // Test deleting a non-existent item
    @Test
    void testDeleteItemNotFound() throws Exception {
        when(itemService.findById(101L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNotFound());
    }
}
