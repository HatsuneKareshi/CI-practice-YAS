package com.yas.product.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PriceValidatorTest {

    private final PriceValidator validator = new PriceValidator();

    @Test
    void isValid_WhenPriceIsPositive_ShouldReturnTrue() {
        assertThat(validator.isValid(10.0, null)).isTrue();
    }

    @Test
    void isValid_WhenPriceIsZero_ShouldReturnTrue() {
        assertThat(validator.isValid(0.0, null)).isTrue();
    }

    @Test
    void isValid_WhenPriceIsNegative_ShouldReturnFalse() {
        assertThat(validator.isValid(-1.0, null)).isFalse();
    }
}
