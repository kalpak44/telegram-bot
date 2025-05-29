package com.home.bot.state.api;

import com.home.model.State;

/**
 * Interface for handling user input based on the current bot state.
 *
 * <p>Each implementation should respond to a specific {@link State} and process the user's input
 * accordingly.
 */
public interface StateInputHandler {

    /**
     * Determines if this handler supports the provided state.
     *
     * @param state the current session state
     * @return true if this handler can process input for the given state
     */
    boolean canHandle(State state);

    /**
     * Processes user input for the associated state.
     *
     * @param chatId the ID of the Telegram chat
     * @param text   the user input to handle
     */
    void handle(Long chatId, String text);
}
