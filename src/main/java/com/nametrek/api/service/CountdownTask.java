package com.nametrek.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CountdownTask {
    private ScheduledFuture<?> scheduledFuture;
    private CompletableFuture<Boolean> countdownCompletion;

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
