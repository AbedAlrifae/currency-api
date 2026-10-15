package com.currency.currency_api.controller;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.currency.currency_api.domain.Currency;
import com.currency.currency_api.service.CurrencyService;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private static final Logger log = LoggerFactory.getLogger(CurrencyController.class);

    private final CurrencyService service;

    public CurrencyController(CurrencyService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Currency> findAll(Pageable pageable) {
        log.info("GET /api/currencies - page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<Currency> page = service.findAll(pageable);
        log.info("GET /api/currencies - {} van {} records teruggegeven",
                page.getNumberOfElements(), page.getTotalElements());
        return page;
    }

    @GetMapping("/{ticker}")
    public Currency findByTicker(@PathVariable String ticker) {
        log.info("GET /api/currencies/{}", ticker);
        return service.findByTicker(ticker);
    }

    @PostMapping
    public ResponseEntity<Currency> create(@RequestBody Currency currency) {
        log.info("POST /api/currencies - ticker={}", currency.getTicker());
        Currency created = service.create(currency);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{ticker}")
                .buildAndExpand(created.getTicker())
                .toUri();
        log.info("POST /api/currencies - aangemaakt: {}", created.getTicker());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{ticker}")
    public Currency update(@PathVariable String ticker, @RequestBody Currency currency) {
        log.info("PUT /api/currencies/{}", ticker);
        return service.update(ticker, currency);
    }

    @DeleteMapping("/{ticker}")
    public ResponseEntity<Void> delete(@PathVariable String ticker) {
        log.info("DELETE /api/currencies/{}", ticker);
        service.delete(ticker);
        return ResponseEntity.noContent().build();
    }

}
