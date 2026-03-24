package io.platformbuilder.depara.mapper;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilialMapperTest {

    @Test
    void deveRetornarListaVaziaQuandoEntradaForNulaOuVazia() {
        assertAll("Deve retornar lista vazia para valores inválidos",
                () -> assertTrue(FilialMapper.parse(null).isEmpty()),
                () -> assertTrue(FilialMapper.parse("").isEmpty()),
                () -> assertTrue(FilialMapper.parse("   ").isEmpty())
        );
    }

    @Test
    void deveParsearFiliaisSeparadasPorPontoEVirgula() {
        List<String> result = FilialMapper.parse("02004015;02004016;02004017");
        assertEquals(Arrays.asList("02004015", "02004016", "02004017"), result);
    }

    @Test
    void deveRemoverEspacosEmBrancoAoRedorDosValores() {
        List<String> result = FilialMapper.parse(" 02004015 ; 02004016 ; 02004017 ");
        assertEquals(Arrays.asList("02004015", "02004016", "02004017"), result);
    }

    @Test
    void deveIgnorarValoresVaziosOuEmBrancoEntreSeparadores() {
        List<String> result = FilialMapper.parse("02004015;; ;02004016;  ;02004017");
        assertEquals(Arrays.asList("02004015", "02004016", "02004017"), result);
    }

    @Test
    void deveManterOrdemDosValores() {
        List<String> result = FilialMapper.parse("B;A;C");
        assertEquals(Arrays.asList("B", "A", "C"), result);
    }

}
