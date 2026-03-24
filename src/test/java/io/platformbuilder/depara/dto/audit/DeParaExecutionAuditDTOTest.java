package io.platformbuilder.depara.dto.audit;

import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DeParaExecutionAuditDTOTest {

    @Test
    void deveCriarDtoComTodosOsDadosInformados() {

        List<DeParaExecutionItemDTO> inseridos = Collections.emptyList();
        List<DeParaExecutionItemDTO> atualizados = Collections.emptyList();

        DeParaExecutionAuditDTO dto = new DeParaExecutionAuditDTO(
                "exec-123",
                DestinoImportacaoEnum.CENTRO_CUSTO,
                "2026-03-19 10:30:00",
                "arquivo-importacao.xlsx",
                100,
                70,
                20,
                10,
                inseridos,
                atualizados
        );

        assertAll("Deve manter os dados informados no construtor",
                () -> assertEquals("exec-123", dto.getExecutionId()),
                () -> assertEquals(DestinoImportacaoEnum.CENTRO_CUSTO, dto.getDestinoImportacao()),
                () -> assertEquals("2026-03-19 10:30:00", dto.getDataHoraExecucao()),
                () -> assertEquals("arquivo-importacao.xlsx", dto.getArquivoOrigem()),
                () -> assertEquals(100, dto.getTotalRecebidos()),
                () -> assertEquals(70, dto.getTotalInseridos()),
                () -> assertEquals(20, dto.getTotalAtualizados()),
                () -> assertEquals(10, dto.getTotalErros()),
                () -> assertSame(inseridos, dto.getRegistrosInseridos()),
                () -> assertSame(atualizados, dto.getRegistrosAtualizados())
        );

    }

}
