package io.platformbuilder.depara.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommandDTOTest {

    @Test
    void deveCriarDtoEManterDadosBasicosCorretamente() {

        CommandDTO dto = new CommandDTO(
                10,
                "Filial SP",
                "Descricao Legado",
                "LEG001",
                "sistema",
                "Descricao Protheus",
                "PRO001"
        );

        assertAll("Deve manter os dados básicos",
                () -> assertEquals(10, dto.getLinhaOrigemExcel()),
                () -> assertEquals("Filial SP", dto.getNomeFilial()),
                () -> assertEquals("Descricao Legado", dto.getDescricaoLegado()),
                () -> assertEquals("LEG001", dto.getCodigoLegado()),
                () -> assertEquals("Descricao Protheus", dto.getDescricaoProtheus()),
                () -> assertEquals("PRO001", dto.getCodigoProtheus())
        );

    }

    @Test
    void deveGerarUniqueKeyCorretamente() {

        CommandDTO dto = new CommandDTO(
                1,
                "FILIAL01",
                null,
                "COD123",
                null,
                null,
                null
        );

        assertEquals("FILIAL01|COD123", dto.uniqueKey());
    }

    @Test
    void deveNormalizarSistemaLegadoAoSetar() {

        CommandDTO dto = new CommandDTO(
                1,
                "FILIAL",
                null,
                "COD",
                null,
                null,
                null
        );

        dto.setSistemaLegado("  protheus  ");

        assertEquals("PROTHEUS", dto.getSistemaLegado());
    }

    @Test
    void deveNormalizarSistemaLegadoAoRecuperarMesmoQuandoSetadoNoConstrutor() {

        CommandDTO dto = new CommandDTO(
                1,
                "FILIAL",
                null,
                "COD",
                "  legado  ",
                null,
                null
        );

        assertEquals("LEGADO", dto.getSistemaLegado());
    }

    @Test
    void deveRetornarNullQuandoSistemaLegadoForNull() {

        CommandDTO dto = new CommandDTO(
                1,
                "FILIAL",
                null,
                "COD",
                null,
                null,
                null
        );

        assertNull(dto.getSistemaLegado());
    }

}
