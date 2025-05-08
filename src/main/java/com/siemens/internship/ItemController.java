package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    /**
     * POST /api/items
     * Creates a new item.

     * Uses @Valid and BindingResult to validate the request body (e.g. email format, non-empty fields)
     * Returns 400 Bad Request if validation fails
     * Returns 201 Created on successful creation
     *  Originally:
     *     - Returned 201 Created even when validation failed
     *     - Returned 400 Bad Request on success
     *     - This has been corrected to follow REST standards ✔
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // << asta e importantă
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    /**
     * GET /api/items/{id}

     * Retrieves an item by its ID.

     * Returns 200 OK if the item is found
     * Returns 404 Not Found if the item does not exist
     * Originally:
     *     - Returned 204 No Content if the item was not found
     *     - 204 is used when a request is successful but there's no content to return,
     *       not when a resource is missing.
     *     - Changed to 404 Not Found to correctly reflect that the resource doesn't exist ✔
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * PUT /api/items/{id}

     * Updates an existing item by ID.

     * - Uses @Valid and BindingResult to validate the input body
     * - Returns 400 Bad Request if validation fails
     * - Returns 200 OK with the updated item if the ID exists
     * - Returns 404 Not Found if the item does not exist

     * Originally:
     * - Returned 201 Created even though the resource already existed
     * - Returned 202 Accepted if item was not found (which is not semantically correct)
     * - The update operation should not return Created or Accepted, but rather OK or Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id,@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * DELETE /api/items/{id}

     * Deletes an item by ID if it exists.

     * - Returns 204 No Content on successful deletion
     * - Returns 404 Not Found if the item does not exist

     * Originally:
     * - Always returned 409 Conflict, regardless of whether the item existed or not
     * - This was incorrect, as 409 implies a conflict in state (e.g., delete not allowed),
     *   not simply that a resource does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemService.findById(id).isPresent()) {
            itemService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * GET /api/items/process

     * Triggers asynchronous processing of all items.

     * - Waits for all asynchronous operations to complete using CompletableFuture.get()
     * - Returns 200 OK with a list of all successfully processed items

     * Originally:
     * - Returned the raw CompletableFuture immediately without awaiting its completion
     * - This could cause inconsistent behavior or incomplete processing on the client side
     */
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() throws Exception {
        List<Item> result = itemService.processItemsAsync().get();
        return ResponseEntity.ok(result);
    }
}
