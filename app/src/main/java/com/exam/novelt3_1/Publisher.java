package com.exam.novelt3_1;

public interface Publisher {
    void addObserver(ConnectionObserver observer);
    void removeObserver(ConnectionObserver observer);
    void notifyObserver();
}
