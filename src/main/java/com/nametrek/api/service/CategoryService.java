package com.nametrek.api.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Store and retrive items in a category
 */
@Service
public class CategoryService {

    private RedisService redisService;

    private String[] categories = {"Animal", "Car", "Country"};

    @Autowired
    public CategoryService(RedisService redisService) {
        this.redisService = redisService;
    }

    public String[] getCategories() {
        return categories;
    }

    /**
     * Add an item to a category 
     *
     * @param category the category to add item to
     * @param item the item 
     */
    public void addItemToCategory(String category, String item) {
        redisService.addToSet(category, item);
    }

    /**
     * Check if an item is present in a category 
     *
     * @param category the category to add item to
     * @param item the item 
     *
     * @return true if item is in set otherwise false
     */
    public boolean isItemInCategory(String category, String item) {
        return redisService.isMemberOfSet(category, item);
    }
}
