package com.yas.product.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProductConverterTest {

    @Test
    void toSlug_ShouldNormalizeWhitespaceAndSymbols() {
        String result = ProductConverter.toSlug("  Macbook Pro 14\" 2026!  ");

        assertThat(result).isEqualTo("macbook-pro-14-2026-");
    }

    @Test
    void toSlug_ShouldRemoveLeadingDashAndCollapseConsecutiveDashes() {
        String result = ProductConverter.toSlug(" ---Hello___World--- ");

        assertThat(result).isEqualTo("hello-world-");
    }
}
