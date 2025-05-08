package com.siemens.internship;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class InternshipApplicationTests {
	@Autowired
	private ItemService itemService;

	@Autowired
	private ItemRepository itemRepository;


	@Test
	void contextLoads() {
	}

}
