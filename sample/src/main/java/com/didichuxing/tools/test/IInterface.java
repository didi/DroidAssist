package com.didichuxing.tools.test;

/**
 * Test interface
 */
public interface IInterface {

    void onCall();

    /**
     * Test interface with generic
     *
     * @param <T> type
     */
    interface Callback<T> {
        void onCallback(final T value);
    }

}
