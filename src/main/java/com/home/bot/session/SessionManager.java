package com.home.bot.session;

import com.home.model.State;
import com.home.model.TimedProductInfo;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user session states and input data. This includes tracking what step the user is in and
 * their partial input (e.g., product details).
 */
public class SessionManager {

    /**
     * Duration after which the session is considered expired.
     */
    public static final Duration SESSION_TTL = Duration.ofHours(1);

    // Maps to store user state and input by chat ID
    private final Map<Long, State> userState = new ConcurrentHashMap<>();
    private final Map<Long, TimedProductInfo> userInput = new ConcurrentHashMap<>();

    /**
     * Clears both session state and input data for the given chat ID.
     *
     * @param chatId Telegram chat ID
     */
    public void clear(Long chatId) {
        userState.remove(chatId);
        userInput.remove(chatId);
    }

    /**
     * Updates the current conversation state (e.g., WAITING_FOR_NAME) for the user.
     *
     * @param chatId Telegram chat ID
     * @param state  New session state to set
     */
    public void updateState(Long chatId, State state) {
        userState.put(chatId, state);
    }

    /**
     * Updates the user's product input data along with the current timestamp.
     *
     * @param chatId           Telegram chat ID
     * @param timedProductInfo Wrapper with product data and timestamp
     */
    public void updateInfo(Long chatId, TimedProductInfo timedProductInfo) {
        userInput.put(chatId, timedProductInfo);
    }

    /**
     * Retrieves the user's product input and its timestamp.
     *
     * @param chatId Telegram chat ID
     * @return TimedProductInfo or null if no input is available
     */
    public TimedProductInfo getInfo(Long chatId) {
        return userInput.get(chatId);
    }

    /**
     * Gets the current session state for the user.
     *
     * @param chatId Telegram chat ID
     * @return State or null if no session is stored
     */
    public State getState(Long chatId) {
        return userState.get(chatId);
    }

    /**
     * Determines whether a session has expired based on timestamp age.
     *
     * @param chatId Telegram chat ID
     * @return true if session is missing or expired
     */
    public boolean isExpired(Long chatId) {
        var timedInfo = getInfo(chatId);
        return timedInfo != null
                && Duration.between(timedInfo.timestamp(), Instant.now()).compareTo(SESSION_TTL) > 0;
    }
}
