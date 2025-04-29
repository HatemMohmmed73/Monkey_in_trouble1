package com.monkeyintrouble.observers;

public interface MonkeyObservable {
    void addObserver(MonkeyObserver observer);
    void removeObserver(MonkeyObserver observer);
}
