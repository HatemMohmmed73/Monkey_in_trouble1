package com.monkeyintrouble.observers;

public interface MonkeyObserver {
    void onHealthChanged(int hearts);
    void onBananaCollected(int totalBananas);
}
