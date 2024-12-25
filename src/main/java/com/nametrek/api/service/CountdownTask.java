package com.nametrek.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

public class CountdownTask {
    private final ScheduledFuture<?> scheduledFuture;
    private final CompletableFuture<Boolean> countdownCompletion;

    public CountdownTask(ScheduledFuture<?> scheduledFuture, CompletableFuture<Boolean> countdownCompletion) {
        this.scheduledFuture = scheduledFuture;
        this.countdownCompletion = countdownCompletion;
    }

    public void cancel() {
        countdownCompletion.complete(true);
        scheduledFuture.cancel(true);
    }

    public CompletableFuture<Boolean> getCompletion() {
        return countdownCompletion;
    }
}
