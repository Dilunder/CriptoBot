package org.example.criptobot.handler;

import org.example.criptobot.service.CryptoService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class CryptoWebSocketHandler extends TextWebSocketHandler {
    private CryptoService cryptoService;

    public CryptoWebSocketHandler(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String symbol = message.getPayload();
        String price = cryptoService.getCryptoPrice(symbol);

        session.sendMessage(new TextMessage(price));
    }
}
