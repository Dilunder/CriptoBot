package org.example.criptobot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class CryptoBot extends TelegramLongPollingBot {

    @Autowired
    private CryptoService cryptoService;
    private final BotConfiguration botConfiguration;

    public CryptoBot(BotConfiguration botConfiguration, CryptoService cryptoService) {
        super(botConfiguration.getToken());
        this.botConfiguration = botConfiguration;
        this.cryptoService = cryptoService;
    }

    @Override
    public void onUpdateReceived(Update update) {

        System.out.println("Received update: " + update);

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // пока что нет необходимости знать от кого пришол запрос
            // String memberName = update.getMessage().getFrom().toString();

            String response;

            if (messageText.equals("/start")) {
                response = "Hello, wich currency do you want to know?" + "\nBTC\nETH\nSOL\nDOGE";
            } else {
                response = cryptoService.getCryptoPrice(messageText.toUpperCase());
            }

            sendMessage(chatId, response);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfiguration.getBotName();
    }

    private void sendMessage(Long chatId, String text){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}