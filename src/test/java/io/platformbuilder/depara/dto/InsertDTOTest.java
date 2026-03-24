package io.platformbuilder.depara.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsertDTOTest {

    @Test
    void deveCriarDtoNormalizandoCamposEManterListaDeFiliais() {

        List<String> filiais = new ArrayList<>(Arrays.asList("02004015", "02004016"));

        InsertDTO dto = new InsertDTO(
                7,
                filiais,
                " EMP01 ",
                " 12345 ",
                " LEG001 ",
                " Descricao teste "
        );

        assertAll("Deve normalizar campos e manter dados corretamente",
                () -> assertEquals(7, dto.getLinhaOrigemExcel()),
                () -> assertEquals(Arrays.asList("02004015", "02004016"), dto.getFiliais()),
                () -> assertEquals("EMP01", dto.getEmpresa()),
                () -> assertEquals("12345", dto.getParaCodigoProtheus()),
                () -> assertEquals("LEG001", dto.getDeCodigoLegado()),
                () -> assertEquals("Descricao teste", dto.getDescricao())
        );
    }

    @Test
    void deveConverterStringsEmBrancoOuVaziasParaNull() {
        InsertDTO dto = new InsertDTO(
                8,
                Collections.singletonList("02004015"),
                "   ",
                "",
                "   ",
                null
        );

        assertAll("Deve converter campos em branco para null",
                () -> assertNull(dto.getEmpresa()),
                () -> assertNull(dto.getParaCodigoProtheus()),
                () -> assertNull(dto.getDeCodigoLegado()),
                () -> assertNull(dto.getDescricao())
        );
    }

    @Test
    void deveUsarListaVaziaQuandoFiliaisForNull() {
        InsertDTO dto = new InsertDTO(
                9,
                null,
                "EMP01",
                "12345",
                "LEG001",
                "Descricao"
        );

        assertNotNull(dto.getFiliais());
        assertTrue(dto.getFiliais().isEmpty());
    }

    @Test
    void deveRetornarListaDeFiliaisNaoModificavel() {
        InsertDTO dto = new InsertDTO(
                10,
                new ArrayList<>(Collections.singletonList("02004015")),
                "EMP01",
                "12345",
                "LEG001",
                "Descricao"
        );

        assertThrows(UnsupportedOperationException.class,
                () -> dto.getFiliais().add("02004016"));
    }

    @Test
    void deveValidarComSucessoQuandoTodosOsCamposObrigatoriosForemInformados() {
        InsertDTO dto = new InsertDTO(
                11,
                Collections.singletonList("02004015"),
                "EMP01",
                "12345",
                "LEG001",
                "Descricao"
        );

        assertDoesNotThrow(() -> dto.validateOrThrow(11));
    }

    @Test
    void deveLancarExcecaoQuandoFiliaisEstiverVazia() {
        InsertDTO dto = new InsertDTO(
                12,
                Collections.emptyList(),
                "EMP01",
                "12345",
                "LEG001",
                "Descricao"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dto.validateOrThrow(12)
        );

        assertEquals("Linha 12: FILIAL é obrigatório.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoEmpresaForNula() {
        InsertDTO dto = new InsertDTO(
                13,
                Collections.singletonList("02004015"),
                null,
                "12345",
                "LEG001",
                "Descricao"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dto.validateOrThrow(13)
        );

        assertEquals("Linha 13: EMPRESA é obrigatório.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoParaCodigoProtheusForNulo() {
        InsertDTO dto = new InsertDTO(
                14,
                Collections.singletonList("02004015"),
                "EMP01",
                null,
                "LEG001",
                "Descricao"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dto.validateOrThrow(14)
        );

        assertEquals("Linha 14: PARA CÓDIGO PROTHEUS é obrigatório.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoDeCodigoLegadoForNulo() {
        InsertDTO dto = new InsertDTO(
                15,
                Collections.singletonList("02004015"),
                "EMP01",
                "12345",
                null,
                "Descricao"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dto.validateOrThrow(15)
        );

        assertEquals("Linha 15: DE CÓDIGO LEGADO é obrigatório.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoDescricaoForNula() {
        InsertDTO dto = new InsertDTO(
                16,
                Collections.singletonList("02004015"),
                "EMP01",
                "12345",
                "LEG001",
                null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dto.validateOrThrow(16)
        );

        assertEquals("Linha 16: DESCRIÇÃO é obrigatório.", exception.getMessage());
    }
}