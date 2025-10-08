package com.pandutimurbhaskara.compose_media.util

/**
 * Generic Undo/Redo manager for state history
 * Supports undo, redo, and maintains a history of states
 *
 * @param T The type of state to manage
 */
class UndoRedoManager<T> {

    private val history = mutableListOf<T>()
    private var currentIndex = -1
    private val maxHistorySize = 50 // Limit to prevent memory issues

    /**
     * Save a new state to history
     * Clears any states after current index (redo history)
     */
    fun saveState(state: T) {
        // Remove any states after current index (clear redo history)
        if (currentIndex < history.size - 1) {
            history.subList(currentIndex + 1, history.size).clear()
        }

        // Add new state
        history.add(state)
        currentIndex++

        // Limit history size
        if (history.size > maxHistorySize) {
            history.removeAt(0)
            currentIndex--
        }
    }

    /**
     * Undo to previous state
     * @return Previous state if available, null otherwise
     */
    fun undo(): T? {
        return if (canUndo()) {
            currentIndex--
            history[currentIndex]
        } else {
            null
        }
    }

    /**
     * Redo to next state
     * @return Next state if available, null otherwise
     */
    fun redo(): T? {
        return if (canRedo()) {
            currentIndex++
            history[currentIndex]
        } else {
            null
        }
    }

    /**
     * Check if undo is available
     */
    fun canUndo(): Boolean = currentIndex > 0

    /**
     * Check if redo is available
     */
    fun canRedo(): Boolean = currentIndex < history.size - 1

    /**
     * Get current state
     */
    fun getCurrentState(): T? = if (currentIndex >= 0 && currentIndex < history.size) {
        history[currentIndex]
    } else {
        null
    }

    /**
     * Clear all history
     */
    fun clear() {
        history.clear()
        currentIndex = -1
    }

    /**
     * Get history size
     */
    fun size(): Int = history.size

    /**
     * Get current index
     */
    fun getCurrentIndex(): Int = currentIndex

    /**
     * Check if history is empty
     */
    fun isEmpty(): Boolean = history.isEmpty()
}
