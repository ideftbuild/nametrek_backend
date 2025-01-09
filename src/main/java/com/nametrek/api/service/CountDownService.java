package com.nametrek.api.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Async
public class CountDownService {
    private static final Map<UUID, CountdownTask> countDownTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;

    public CountDownService() {
        System.out.println("creating an instance of scheduler");
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Start a count down
     *
     * @param start   the number to start with
     * @param roomId  the room id
     * @param onTick  A Consumer that can be used to print the current count
     *
     * @return A promise that will be resolved to true if stopped explicitly otherwise false when completed
     */
    public CompletableFuture<Boolean> startCountDown(int start, UUID roomId, Consumer<Integer> onTick) {
        // First, stop any existing countdown for this room
		if (roomId != null) {
			stopCountDown(roomId);
		}
        
        AtomicInteger count = new AtomicInteger(start);
        CompletableFuture<Boolean> countdownCompletion = new CompletableFuture<>();
        AtomicReference<ScheduledFuture<?>> futureRef = new AtomicReference<>();

        ScheduledFuture<?> future = this.scheduler.scheduleAtFixedRate(() -> {
            try {
                int currentCount = count.getAndDecrement();
                if (currentCount >= 0) {
                    onTick.accept(currentCount);
                } else {
					 // Get the future from the AtomicReference
                    ScheduledFuture<?> currentFuture = futureRef.get();
                    if (currentFuture != null && !currentFuture.isCancelled()) {
                        currentFuture.cancel(false);
                    }
                    
                    if (roomId != null) {
                        countDownTasks.remove(roomId);
                    }
                    countdownCompletion.complete(false);
                }
            } catch (Exception e) {
                // Handle any exceptions in the scheduled task
                countdownCompletion.completeExceptionally(e);
            }
        }, 0, 1, TimeUnit.SECONDS);

        // Store the future in the AtomicReference
        futureRef.set(future);

        if (roomId != null) {
            CountdownTask newTask = new CountdownTask(future, countdownCompletion);
            countDownTasks.put(roomId, newTask);
            System.out.println("Storing count down task in hash with key: " + roomId);
            System.out.println("After storing: " + countDownTasks);
        }

        return countdownCompletion;
    }

    /**
     * Stops a count down
     *
     * @param roomId the roomId
     */
    public void stopCountDown(UUID roomId) {
        System.out.println("Stop count down is called");
        // CountdownTask countdownTask = countDownTasks.get(roomId);
		try {
			System.out.println("count down tasks are : " + countDownTasks);
			CountdownTask countdownTask = countDownTasks.remove(roomId);
			if (countdownTask != null) {
				countdownTask.cancel();
				// System.out.println("Successfully cancelled and removed countdown task for room: " + roomId);
			}
		} catch (Exception e) {	
			e.printStackTrace();
		}
        

    }

    // Add this method to help with debugging
    public boolean hasActiveCountdown(UUID roomId) {
        CountdownTask task = countDownTasks.get(roomId);
        return task != null && !task.getScheduledFuture().isDone() && !task.getScheduledFuture().isCancelled();
    }
}
