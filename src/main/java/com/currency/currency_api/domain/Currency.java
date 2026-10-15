package com.currency.currency_api.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import jakarta.persistence.*;

@Entity
public class Currency {
    @Id
    private String ticker;
    private String name;
    private BigInteger numberOfCoins;
    private BigDecimal marketCap;

    protected Currency() {
    }

    private Currency(Builder builder) {
        this.ticker = builder.ticker;
        this.name = builder.name;
        this.numberOfCoins = builder.numberOfCoins;
        this.marketCap = builder.marketCap;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public BigInteger getNumberOfCoins() {
        return numberOfCoins;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public static final class Builder {

        private String ticker;
        private String name;
        private BigInteger numberOfCoins;
        private BigDecimal marketCap;

        public Builder ticker(String ticker) {
            this.ticker = ticker;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder numberOfCoins(BigInteger numberOfCoins) {
            this.numberOfCoins = numberOfCoins;
            return this;
        }

        public Builder marketCap(BigDecimal marketCap) {
            this.marketCap = marketCap;
            return this;
        }

        public Currency build() {
            if (ticker == null || ticker.isBlank()) {
                throw new IllegalArgumentException("ticker is verplicht");
            }
            return new Currency(this);
        }
    }
}