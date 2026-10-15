package com.currency.currency_api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import com.currency.currency_api.domain.Currency;
import com.currency.currency_api.repository.CurrencyRepository;

import com.currency.currency_api.service.CurrencyService;

public class CurrencyServiceTest {
    private CurrencyRepository repository;
    private CurrencyService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CurrencyRepository.class);
        service = new CurrencyService(repository);
    }

    @Test
    void findByTicker_geeftDeValutaTerugAlsDieBestaat() {
        Currency currency = Currency.builder().ticker("LTC").name("Litecoin").build();
        when(repository.findById("LTC")).thenReturn(Optional.of(currency));

        Currency result = service.findByTicker("LTC");

        assertThat(result.getName()).isEqualTo("Litecoin");
    }

    @Test
    void findByTicker_gooitNotFoundAlsDeValutaNietBestaat() {
        when(repository.findById("onbekend")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByTicker("onbekend"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void create_slaatNietsOpAlsDeTickerAlBestaat() {
        Currency currency = Currency.builder().ticker("LTC").name("Litecoin").build();
        when(repository.existsById("LTC")).thenReturn(true);

        assertThatThrownBy(() -> service.create(currency))
                .isInstanceOf(ResponseStatusException.class);

        verify(repository, never()).save(any(Currency.class));
    }
}
