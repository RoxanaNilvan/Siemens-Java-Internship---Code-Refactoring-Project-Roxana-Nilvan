package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        // Step 1: Get all item IDs from the database
        List<Long> itemIds = itemRepository.findAllIds();

        // Step 2: Prepare a list to hold async processing results
        List<CompletableFuture<Item>> futures = new ArrayList<>();

        for (Long id : itemIds) {
            // Step 3: For each item ID, process it asynchronously
            CompletableFuture<Item> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // Try to retrieve item by ID
                    Optional<Item> optionalItem = itemRepository.findById(id);
                    if (optionalItem.isPresent()) {
                        // If item exists, update status and save it
                        Item item = optionalItem.get();
                        item.setStatus("PROCESSED");
                        return itemRepository.save(item);
                    } else {
                        // Item not found — log warning (no exception thrown)
                        System.err.println("Item with ID " + id + " not found.");
                    }
                } catch (Exception e) {
                    // Catch and log any unexpected exception
                    System.err.println("Error processing item with ID " + id + ": " + e.getMessage());
                }
                // Return null if processing failed
                return null;
            }, executor);

            futures.add(future);
        }

        // Step 4: Wait for all async tasks to complete
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<Item> processed = new ArrayList<>();
                    for (CompletableFuture<Item> f : futures) {
                        try {
                            // Try to get the result of each future
                            Item result = f.get();
                            if (result != null) {
                                processed.add(result);
                            }
                        } catch (Exception e) {
                            // Log failure to retrieve result
                            System.err.println("Failed to get result: " + e.getMessage());
                        }
                    }
                    // Return list of all successfully processed items
                    return processed;
                });
    }

}

