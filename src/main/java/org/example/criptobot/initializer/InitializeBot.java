package org.example.criptobot.initializer;

import org.example.criptobot.pojo.CryptoBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.GetWebhookInfo;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class InitializeBot {

    @Autowired
    private CryptoBot cryptoBot;

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(cryptoBot);
            System.out.println("Bot registered successfully.");

            GetWebhookInfo getWebhookInfo = new GetWebhookInfo();
            WebhookInfo webhookInfo = cryptoBot.execute(getWebhookInfo);
            if (webhookInfo != null && webhookInfo.getUrl() != null) {
                System.out.println("Webhook exists, continuing...");
            } else {
                System.out.println("Webhook does not exist, continuing...");
            }
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("Error removing old webhook")) {

                System.out.println("Webhook does not exist, continuing...");
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}