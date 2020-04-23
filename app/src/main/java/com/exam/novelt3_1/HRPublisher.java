package com.exam.novelt3_1;

public interface HRPublisher {
    void addObserver(HeartRateObserver observer);
    void removeObserver(HeartRateObserver observer);
    void notifyObserver_HR();
}
