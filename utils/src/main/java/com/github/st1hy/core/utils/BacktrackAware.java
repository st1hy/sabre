package com.github.st1hy.core.utils;

public interface BacktrackAware {
    /**
     * Allows fragment to decide if it wants to prevent backtrack
     * @return true to stop further backtrack processing as backtrack was handled by fragment, otherwise false
     */
    boolean handleBacktrackEvent();
}
