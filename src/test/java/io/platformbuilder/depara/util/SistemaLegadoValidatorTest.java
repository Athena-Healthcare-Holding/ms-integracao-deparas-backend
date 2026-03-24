package io.platformbuilder.depara.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SistemaLegadoValidatorTest {

    @Test
    void deveValidarSistemasLegadosCorretamente() {

        assertAll("Deve reconhecer sistemas válidos independente de formatação",
                () -> assertTrue(SistemaLegadoValidator.isValido("SOLUS")),
                () -> assertTrue(SistemaLegadoValidator.isValido("solus")),
                () -> assertTrue(SistemaLegadoValidator.isValido("  mv  ")),
                () -> assertTrue(SistemaLegadoValidator.isValido("TaSy")),

                () -> assertFalse(SistemaLegadoValidator.isValido(null)),
                () -> assertFalse(SistemaLegadoValidator.isValido("")),
                () -> assertFalse(SistemaLegadoValidator.isValido("   ")),
                () -> assertFalse(SistemaLegadoValidator.isValido("SAP")),
                () -> assertFalse(SistemaLegadoValidator.isValido("OUTRO"))
        );

    }

    @Test
    void deveNormalizarSistemaLegadoCorretamente() {

        assertAll("Deve normalizar valores para trim + uppercase",
                () -> assertEquals("SOLUS", SistemaLegadoValidator.normalizar("solus")),
                () -> assertEquals("MV", SistemaLegadoValidator.normalizar("  mv  ")),
                () -> assertEquals("TASY", SistemaLegadoValidator.normalizar("TaSy")),

                () -> assertNull(SistemaLegadoValidator.normalizar(null)),
                () -> assertNull(SistemaLegadoValidator.normalizar("")),
                () -> assertNull(SistemaLegadoValidator.normalizar("   "))
        );

    }

}
