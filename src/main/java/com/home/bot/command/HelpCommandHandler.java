package com.home.bot.command;

import com.home.bot.command.api.CommandHandler;
import com.home.bot.common.api.MessageSender;

/**
 * Handles the /help command by displaying usage instructions to the user.
 */
public class HelpCommandHandler implements CommandHandler {

    private final MessageSender messageSender;

    /**
     * Constructs a HelpCommandHandler with the provided message sender.
     *
     * @param messageSender the message sender used to send help information
     */
    public HelpCommandHandler(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Checks if this handler supports the given command.
     *
     * @param command user input text
     * @return true if the command is "/help"
     */
    @Override
    public boolean canHandle(String command) {
        return "/help".equalsIgnoreCase(command);
    }

    /**
     * Sends the help message with usage instructions.
     *
     * @param chatId Telegram chat ID to send the message to
     */
    @Override
    public void handle(Long chatId) {
        messageSender.send(chatId,
                """
                        🤖 *PaymentBot Help*
                        
                        Available commands:
                        /start – Begin payment link creation
                        /cancel – Cancel current operation
                        /status – Check your current input progress
                        /help – Show this help message
                        
                        💡 Input guidance:
                        - After /start, provide the price and currency (e.g. `10.00 USD`)
                          ↳ Supported currencies: https://docs.stripe.com/currencies
                        - Then, enter the product name
                        - Finally, provide the quantity
                        
                        The bot will return a Stripe payment link based on your input.
                        """);
    }
}
