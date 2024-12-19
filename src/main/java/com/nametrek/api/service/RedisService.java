package com.nametrek.api.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
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
     * Set a field in a hash
     *
     * @param hash The hash
     * @param object The object to set
     */
    public <T extends Identifiable> void setField(String hash, T object) {
        String key = object.getId();
        template.opsForHash().put(hash, key, object);
    }

    /**
     * Get a field from a hash
     *
     * @param hash The hash
     * @param key The key for the field
     */
    public Object getField(String hash, String key) {
        return template.opsForHash().get(hash, key);
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
    public <T extends Scorable> void addToSortedSet(String key, T member, Integer score) {
        template.opsForZSet().add(key, member, score);
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
     * Deletes a member from a sorted set.
     *
     * @param key the key of the sorted set
     * @param member the member to delete
     *
     * @return a number greater than 0 on success
     */
    public <T extends Scorable> Long deleteMemberFromSortedSet(String key, T member) {
        return template.opsForZSet().remove(key, member, member.getScore());
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
     * Checks if a key is present in a hash.
     *
     * @param hash the hash to search
     * @param key  the key to check for
     *
     * @return true if key is present otherwise false
     */
    public boolean hasKey(String hash, String key) {
        return template.opsForHash().hasKey(hash, key);
    }
}


