package org.example.criptobot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CryptoPriceService {

    private final CryptoService cryptoService;

    @Cacheable(value = "coins", key = "#symbol")
    public String getById(String symbol) throws InterruptedException {
        Thread.sleep(5000);
        return cryptoService.getCryptoPrice(symbol);
    }
}
