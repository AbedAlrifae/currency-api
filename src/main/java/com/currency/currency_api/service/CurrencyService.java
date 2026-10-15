package com.currency.currency_api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.currency.currency_api.domain.Currency;
import com.currency.currency_api.repository.CurrencyRepository;

@Service
public class CurrencyService {
    private final CurrencyRepository repository;

    public CurrencyService(CurrencyRepository repository) {
        this.repository = repository;
    }

    public Page<Currency> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Currency findByTicker(String ticker) {
        return repository.findById(ticker)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Valuta niet gevonden: " + ticker));
    }

    public Currency create(Currency currency) {
        if (repository.existsById(currency.getTicker())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Valuta bestaat al: " + currency.getTicker());
        }
        return repository.save(currency);
    }

    public Currency update(String ticker, Currency currency) {
        if (!repository.existsById(ticker)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Valuta niet gevonden: " + ticker);
        }
        return repository.save(currency);
    }

    public void delete(String ticker) {
        if (!repository.existsById(ticker)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Valuta niet gevonden: " + ticker);
        }
        repository.deleteById(ticker);
    }
}
