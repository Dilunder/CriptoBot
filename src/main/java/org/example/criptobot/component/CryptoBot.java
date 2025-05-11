package org.example.criptobot.component;

import org.example.criptobot.coins.Coin;
import org.example.criptobot.handler.CryptoWebSocketHandler;
import org.example.criptobot.config.tgbot.BotConfiguration;
import org.example.criptobot.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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

                StringBuilder coinsList = new StringBuilder("Hello, which currency do you want to know?\n");

                for (Coin coin : Coin.values()) {
                    coinsList.append(coin.name()).append("\n");
                }
                sendMessage(chatId, coinsList.toString());
            } else {
                getPriceWebSocket(chatId, messageText);
            }
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void updateCoin() {
        cryptoService.evictAllCacheValues();

        for (Coin coin : Coin.values()) {
            cryptoService.getCryptoPrice(coin.name().toLowerCase());
        }
    }

    public String getBinanceId(String symbol) {
        symbol = symbol.toUpperCase();

        try {
            Coin coin = Coin.valueOf(symbol);
            return coin.name() + "USDT";
        } catch (IllegalArgumentException e) {
            return "Coin Not Found";
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