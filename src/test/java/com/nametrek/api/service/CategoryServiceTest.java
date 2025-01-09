package com.nametrek.api.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    // @Mock
    // private RedisService redisService;
    //
    // @InjectMocks
    // private CategoryService categoryService;
    //
    //
    // String category = "animal";
    //
    // String item = "cat";
    //
    // /**
    //  * Verify that the method adds an item to a category
    //  */
    // @Test
    // public void testAddItemToCategry() {
    //     doNothing().when(redisService).addToSet(category, item);
    //
    //     categoryService.addItemToCategory(category, item);
    //
    //     verify(redisService).addToSet(category, item);
    // }
    //
    // /**
    //  * Test that the method returns true when an item is present in a category
    //  */
    // @Test
    // public void testIsItemInCategory() {
    //     when(redisService.isMemberOfSet(category, item)).thenReturn(true);
    //
    //     Boolean result = categoryService.isItemInCategory(category, item);
    //
    //     verify(redisService).isMemberOfSet(category, item);
    //     assertTrue(result);
    // }
}
