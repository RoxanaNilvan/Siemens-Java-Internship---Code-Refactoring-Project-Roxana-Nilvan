package com.siemens.internship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository; // Mocked repository for isolating service logic

    @InjectMocks
    private ItemService itemService; // The service under test

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks before each test
    }

    // Tests retrieval of all items
    @Test
    void testFindAll() {
        Item item1 = new Item();
        item1.setName("Item1");
        Item item2 = new Item();
        item2.setName("Item2");

        when(itemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        List<Item> result = itemService.findAll();
        assertEquals(2, result.size());
    }

    // Tests finding an item by ID when it exists
    @Test
    void testFindByIdFound() {
        Item item = new Item();
        item.setId(100L);
        item.setName("Test");

        when(itemRepository.findById(101L)).thenReturn(Optional.of(item));

        Optional<Item> result = itemService.findById(101L);
        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getName());
    }

    // Tests finding an item by ID when it does not exist
    @Test
    void testFindByIdNotFound() {
        when(itemRepository.findById(404L)).thenReturn(Optional.empty());

        Optional<Item> result = itemService.findById(404L);
        assertFalse(result.isPresent());
    }

    // Tests saving a new item
    @Test
    void testSave() {
        Item item = new Item();
        item.setName("New Item");

        when(itemRepository.save(item)).thenReturn(item);

        Item result = itemService.save(item);
        assertEquals("New Item", result.getName());
    }

    // Tests deleting an item by ID
    @Test
    void testDeleteById() {
        Long idToDelete = 123L;

        doNothing().when(itemRepository).deleteById(idToDelete);

        itemService.deleteById(idToDelete);

        verify(itemRepository, times(1)).deleteById(idToDelete);
    }

    // Tests async processing when all items exist and are processed
    @Test
    void testProcessItemsAsync_AllItemsProcessed() throws Exception {
        Item item1 = new Item();
        item1.setId(201L);
        item1.setStatus("NEW");

        Item item2 = new Item();
        item2.setId(202L);
        item2.setStatus("NEW");

        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(201L, 202L));
        when(itemRepository.findById(201L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(202L)).thenReturn(Optional.of(item2));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processed = future.get();

        assertEquals(2, processed.size());
        assertTrue(processed.stream().allMatch(i -> "PROCESSED".equals(i.getStatus())));
    }

    // Tests async processing when one item is missing (should skip it)
    @Test
    void testProcessItemsAsync_ItemNotFound() throws Exception {
        Item item1 = new Item();
        item1.setId(301L);
        item1.setStatus("NEW");

        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(301L, 302L));
        when(itemRepository.findById(301L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(302L)).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processed = future.get();

        assertEquals(1, processed.size());
        assertEquals("PROCESSED", processed.get(0).getStatus());
    }
}
