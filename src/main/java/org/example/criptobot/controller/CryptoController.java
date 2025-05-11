package org.example.criptobot.controller;

import lombok.RequiredArgsConstructor;
import org.example.criptobot.coins.Coin;
import org.example.criptobot.service.CryptoPriceService;
import org.example.criptobot.service.CryptoService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class CryptoController {

    private final CryptoPriceService cryptoPrice;
    private final CryptoService cryptoService;

    @GetMapping("/getPrice/{symbol}")
    public ResponseEntity<String> getPrice(@PathVariable(required = false) String symbol){
        try {
            return ResponseEntity.ok(cryptoPrice.getById(symbol));
        } catch (Exception e){
            System.out.println("Error message " + e.getMessage());
            return ResponseEntity.ok("smth wrong");
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void updateCoin(){

        cryptoService.evictAllCacheValues();

        for (Coin coin : Coin.values()) {
            cryptoService.getCryptoPrice(coin.name().toLowerCase());
        }
    }
}