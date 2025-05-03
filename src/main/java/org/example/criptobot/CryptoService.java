package org.example.criptobot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
public class CryptoService extends TextWebSocketHandler {

    private final String API_URL = "https://api.binance.com/api/v3/ticker/price?symbol=";
    private static final RestTemplate restTemplate = new RestTemplate();

    public String getCryptoPrice(String symbol){
        try{
            String url = HttpClient
                    .newHttpClient()
                    .send(HttpRequest
                            .newBuilder()
                            .uri(URI.create(API_URL + getBinanceId(symbol)))
                            .build(),
                        HttpResponse
                            .BodyHandlers
                            .ofString())
                    .body();

            String price = new ObjectMapper()
                    .readTree(url)
                    .get("price")
                    .asText();

            if (url != null){
                float convertedPrice = Float.parseFloat(price);

                StringBuilder currencyInfo = new StringBuilder()
                        .append(getBinanceId(symbol))
                        .append(": ")
                        .append(convertedPrice)
                        .append(" usd");

                return currencyInfo.toString();
            } else {
                return "not found";
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            return "Error while trying to get final currency value";
        }
    }

    private WebSocketSession binanceSession;

    ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, String> latestPrice = new ConcurrentHashMap<>();
    private Map<String, String> symbolMapper = new ConcurrentHashMap<>();

    public CryptoService() {
        connectToBinance();
    }

    public void connectToBinance(){
        WebSocketClient webSocketClient = new StandardWebSocketClient();

        try{
            binanceSession = webSocketClient.execute(this, URI.create(binanceUrl).toString()).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connected to binance");
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        System.out.println("WebSocket error: " + exception);
        reconnect();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

        System.out.println("Disconnect from binance");

        reconnect();
    }

    public void reconnect(){
        try{
            Thread.sleep(5000);
            connectToBinance();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLatestPrice(String symbol){
        String streamName = getBinanceStreamName(symbol);
        String price = latestPrice.get(streamName);

        return price != null ? String.format("%s: %s", streamName.toUpperCase(), price) : "Price not available";
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());

        if (jsonNode.has("s") && jsonNode.has("c")){
            String symbol = jsonNode.get("s").asText().toLowerCase();
            String currentPrice = jsonNode.get("c").asText();

            latestPrice.put(symbol, currentPrice);
        }
    }

    private String getBinanceStreamName(String currency){
        return symbolMapper.getOrDefault(currency.toUpperCase(), currency.toLowerCase());
    }

    private Map<String, String> getBinanceId(){

        Map<String, String> mapppings = new HashMap<>();

        mapppings.put("BTC", "btcusdt");
        mapppings.put("ETH", "ethusdt");
        mapppings.put("SOL", "solusdt");
        mapppings.put("DOGE", "dogeusdt");

        return mapppings;
    }
}
