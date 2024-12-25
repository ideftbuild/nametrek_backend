package com.nametrek.api.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Async
public class CountDownService {

    private final Map<String, CountdownTask> countDownTasks = new ConcurrentHashMap<>();

    /**
     * Start a count down
     * 
     * @param start the number to start with
     * @param roomId the room id
     * @param onTick A Consumer that can be used to print the current count
     *
     * @return A promise that will be resolved to true if stopped explicitly otherwise false when completed
     */
    public CompletableFuture<Boolean> startCountDown(int start, String roomId, Consumer<Integer> onTick) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger count = new AtomicInteger(start);

        // Create a CompletableFuture to track the countdown completion
        CompletableFuture<Boolean> countdownCompletion = new CompletableFuture<>();

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            int currentCount = count.getAndDecrement();
            if (currentCount > 0) {
                onTick.accept(currentCount);
            } else {
                if (roomId != null) {
                    countDownTasks.remove(roomId);
                }
                scheduler.shutdown();
                countdownCompletion.complete(false); // Mark countdown as completed
            }
        }, 0, 1, TimeUnit.SECONDS);

        // Store the future in the HashMap
        if (roomId != null) {
            // countDownTasks.put(roomId, new AbstractMap.SimpleEntry<>(future, countdownCompletion));
            countDownTasks.put(roomId, new CountdownTask(future, countdownCompletion));
        }

        // Return the CompletableFuture for the countdown
        return countdownCompletion;
    }

    /**
     * Stops a count down
     *
     * @param roomId the roomId
     */
    public void stopCountDown(String roomId) {
        CountdownTask countdownTask = countDownTasks.get(roomId);
        if (countdownTask != null) {
            countdownTask.cancel();
            countDownTasks.remove(roomId);
        }
    }
}
