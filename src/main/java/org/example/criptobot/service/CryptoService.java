package org.example.criptobot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.criptobot.coins.Coin;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class CryptoService {
    private final String API_URL = "https://api.binance.com/api/v3/ticker/price?symbol=";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Cacheable(value = "CryptoPrice", key = "#symbol.toUpperCase()")
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

        try {
            Coin coin = Coin.valueOf(symbol);
            return coin.name() + "USDT";
        } catch (IllegalArgumentException e) {
            return "Coin Not Found";
        }
    }

    @CacheEvict(value = "CryptoPrice", allEntries = true)
    public void evictAllCacheValues(){
    }
}