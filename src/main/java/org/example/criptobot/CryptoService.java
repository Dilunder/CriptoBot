package org.example.criptobot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CryptoService {

    private final String API_URL = "https://api.binance.com/api/v3/ticker/price?symbol=";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getCryptoPrice(String symbol){
        try{
            String response = restTemplate.getForObject(API_URL + getBinanceId(symbol), String.class);

            String price = objectMapper
                    .readTree(response)
                    .get("price")
                    .asText();

            return String.format("%s: %s", getBinanceId(symbol), price) + " usd";

        } catch (Exception e){
            return "Error while trying to get final currency value";
        }
    }

    public String getBinanceId(String symbol){

        symbol = symbol.toUpperCase();

        switch (symbol){
            case "BTC" -> {
                return symbol + "USDT";
            }
            case "ETH" -> {
                return symbol + "USDT";
            }
            case "SOL" -> {
                return symbol + "USDT";
            }
            case "DOGE" -> {
                return symbol + "USDT";
            }
            default -> {
                return "Coin Not Found";
            }
        }
    }
}
