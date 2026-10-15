package com.currency.currency_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.currency.currency_api.domain.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, String> {

}
