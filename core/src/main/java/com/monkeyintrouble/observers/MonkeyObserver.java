package com.monkeyintrouble.observers;

public interface MonkeyObserver {
    void onHealthChanged(int newHealth);
    void onBananaCollected(int totalBananas);
    void onGhostModeChanged(boolean isGhostMode);
}
