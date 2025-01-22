package com.nametrek.api.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.nametrek.api.model.Identifiable;
import com.nametrek.api.model.Scorable;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisService {

    public RedisTemplate<String, Object> template;


    @Autowired
    public RedisService(RedisTemplate<String, Object> template) {
        this.template = template;
    }

    /**
     * Set a value
     *
     * @param key the key
     * @param value the value
     */
    public <T> void setValue(String key, T value) {
        template.opsForValue().set(key, value);
    }

    /**
     * Set a value with an expiration time
     *
     * @param key the key
     * @param value the value
     * @param timeout expiration time
     * @param timeUnit the unit
     */
    public <T> void setValueExp(String key, T value, long timeout, TimeUnit timeUnit) {
        try {
            template.opsForValue().set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }

    public <T> Double incrementValue(String key, T member, Double delta) {
        return template.opsForZSet().incrementScore(key, member, delta);
    }

    // public Long decrementValue(String hash, String key) {
    //     return template.opsForHash().increment(key, key, -1);
    // }

    /**
     * Get a value and delete from database
     *
     * @param key the key
     *
     * @return the object that is mapped to key
     */
    public Object getAndDelete(String key) {
        return template.opsForValue().getAndDelete(key);
    }

    /**
     * Get a value 
     *
     * @param key the key
     *
     * @return the object that is mapped to key
     */
    public Object getValue(String key) {
        return template.opsForValue().get(key);
    }

    /**
     * Delete a value
     *
     * @param key the key
     */
    public boolean delete(String key) {
        return template.delete(key);
    }

     
    /**
     * Set a field in a hash
     *
     * @param hash The hash
     * @param object The object to set
     */
    public <T> void setField(String hash, String key, T object) {
        template.opsForHash().put(hash, key, object);
    }

    /**
     * Set multiple fields in a hash
     *
     * @param hash The hash
     * @param fields The fields to set
     */
    public void setFields(String hash, Map<String, Object> fields) {
        if (fields != null && !fields.isEmpty()) {
            template.opsForHash().putAll(hash, fields);
        }
    }

    //
    /**
     * Get a field from a hash
     *
     * @param hash The hash
     * @param key The key for the field
     */
    public Object getField(String hash, String key) {
        return template.opsForHash().get(hash, key);
    }

    public List<Object> getFields(String hash, Collection<Object> fields) {
        return template.opsForHash().multiGet(hash, fields);
    }

    /**
     * Delete a field from a hash
     *
     * @param hash The hash
     * @param key The key for the field
     * @return number greater than 0 if successful
     */
    public Long deleteField(String hash, String key) {
        return template.opsForHash().delete(hash, key);
    }

    /**
     * Add a member to a sorted set 
     *
     * @param key The key for the field
     * @param member The member to add
     * @param score The score used for sorting
     */
    public <T> void addToSortedSet(String key, T member, Double score) {
        template.opsForZSet().add(key, member, score);
    }

    public <T> Double getMemberScore(String key, T member) {
        return template.opsForZSet().score(key, member);
    }

    /**
     * Get member from a sorted set
     *
     * @param order the order to get the members from the sorted set
     * @param key   the sorted set key
     * @return      the a set of the members in order
     */
    public Set<Object> getSortedSet(String order, String key)
    {
        if (order.equals("DESC")) {
            return template.opsForZSet().reverseRange(key, 0, -1);
        } else {
            return template.opsForZSet().range(key, 0, -1);
        }
    } 

    /**
     * Get member from a sorted set
     *
     * @param order the order to get the members from the sorted set
     * @param key   the sorted set key
     * @return      the a set of the members in order
     */
    public Set<TypedTuple<Object>> getSortedSetWithScores(String order, String key)
    {
        if (order.equals("DESC")) {
            return template.opsForZSet().reverseRangeWithScores(key, 0, -1);
        } else {
            return template.opsForZSet().rangeWithScores(key, 0, -1);
        }
    } 

    /**
     * Deletes a member from a sorted set.
     *
     * @param key the key of the sorted set
     * @param member the member to delete
     *
     * @return a number greater than 0 on success
     */
    public <T> Long deleteMemberFromSortedSet(String key, T member) {
        return template.opsForZSet().remove(key, member);
    }

    /**
     * Updates a member in a sorted set and its associated hash.
     * 
     * This method updates the fields of a specified member in both the sorted set
     * identified by {@code setKey} and the hash associated with it. The outdated member
     * is replaced with the updated version in both data structures.
     * 
     * @param setKey    the key identifying the sorted set to update
     * @param hash      the hash containing the fields of the members
     * @param oldMember the existing member with outdated fields to be replaced
     * @param newMember the new member containing updated fields
     */
    public <T extends Scorable & Identifiable> void updateSortedSetMemberAndHash(
            String setKey,
            String hash,
            T oldMember,
            T newMember) {

        String luaScript =
            "local sanitizedKey = string.gsub(ARGV[3], '^\\\"(.*)\\\"$', '%1');" +
            "redis.call('ZREM', KEYS[1], ARGV[1]);" +
            "redis.call('ZADD', KEYS[1], ARGV[2], ARGV[4]);" + 
            "redis.call('HSET', KEYS[2], sanitizedKey, ARGV[4]);";

        template.execute(
            new DefaultRedisScript<>(luaScript, Void.class),
            List.of(setKey, hash),
            oldMember,
            newMember.getScore().doubleValue(),
            newMember.getId(),
            newMember);
    }
    
    /**
     * Deletes a member from a sorted set and its associated hash.
     *
     * @param setKey the key identifying the sorted set
     * @param hash   the hash containing the member's fields
     * @param member the member to be deleted
     */
    public <T extends Scorable & Identifiable> void deleteMemberFromSortedSetAndHash(String setKey, String hash, T member) {
        String luaScript = 
            "local sanitizedKey = string.gsub(ARGV[1], '^\\\"(.*)\\\"$', '%1');" +
            "redis.call('ZREM', KEYS[1], ARGV[2]);" +
            "redis.call('HDEL', KEYS[2], sanitizedKey);";

        template.execute(
                new DefaultRedisScript<>(luaScript, Void.class),
                List.of(setKey, hash),
                member.getId(),
                member);
    }

    /**
     * Checks if a field is present in a hash using a key
     *
     * @param hash the hash to search
     * @param key  the key to check for
     *
     * @return true if field is present otherwise false
     */
    public boolean fieldExists(String hash, String key) {
        return template.opsForHash().hasKey(hash, key);
    }


    /**
     * Checks if a key exists
     *
     * @param key th key to check for
     * @return true if key is present otherwise false
     */
    public boolean keyExists(String key) {
        return template.hasKey(key);
    }

    /**
     * Add a member to a set
     *
     * @param key the key to the set
     * @param member  the new member to add to the set
     */
    public void addToSet(String key, String member) {
        template.opsForSet().add(key, member);
    }

    /**
     * Checks if member is present in a set
     *
     * @param key the key to the set
     * @param member  the new member to add to the set
     *
     * @return true if member is in set otherwise false
     */
    public boolean isMemberOfSet(String key, String member) {
        return template.opsForSet().isMember(key, member);
    }

    public Long sortedSetLength(String key) {
        return template.opsForZSet().size(key);
    }

    // public Long deleteFields(String key) {
    //     return template.opsForHash().delete
    // }
}


