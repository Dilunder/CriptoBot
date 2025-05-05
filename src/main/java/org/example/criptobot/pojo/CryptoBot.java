package org.example.criptobot.pojo;

import org.example.criptobot.handler.CryptoWebSocketHandler;
import org.example.criptobot.config.BotConfiguration;
import org.example.criptobot.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
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

            if (messageText.equals("/start")) {
                sendMessage(chatId, "Hello, wich currency do you want to know?" + "\nBTC\nETH\nSOL\nDOGE");
            } else {
                getPriceWebSocket(chatId, messageText);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botConfiguration.getBotName();
    }

    public void getPriceWebSocket(Long chatId, String symbol){
        StandardWebSocketClient client = new StandardWebSocketClient();

        WebSocketConnectionManager connectionManager = new WebSocketConnectionManager(
                client,
                new CryptoWebSocketHandler(cryptoService),
                "ws://localhost:8080/crypto-price");

        connectionManager.start();

        try {
            String response = cryptoService.getCryptoPrice(symbol);
            sendMessage(chatId, response);
        } finally {
            connectionManager.stop();
        }
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