package io.platformbuilder.depara.util;

import io.platformbuilder.depara.dto.CommandDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleExecutionScriptBuilderTest {

    private CommandDTO buildCommand() {
        return new CommandDTO(
                1,
                "FILIAL01",
                "Descricao Legado",
                "LEG001",
                "protheus",
                "Descricao Protheus",
                "PRO001"
        );
    }

    @Test
    void deveGerarInsertCorretamenteParaCentroCusto() {

        CommandDTO command = buildCommand();

        String sql = OracleExecutionScriptBuilder.buildInsert(
                DestinoImportacaoEnum.CENTRO_CUSTO,
                command
        );

        assertAll("Deve gerar INSERT corretamente",
                () -> assertTrue(sql.contains("INSERT INTO NEXTDIGITAL.CENTRO_CUSTO")),
                () -> assertTrue(sql.contains("NOME_FILIAL")),
                () -> assertTrue(sql.contains("'FILIAL01'")),
                () -> assertTrue(sql.contains("'Descricao Legado'")),
                () -> assertTrue(sql.contains("'LEG001'")),
                () -> assertTrue(sql.contains("'PROTHEUS'")),
                () -> assertTrue(sql.contains("'Descricao Protheus'")),
                () -> assertTrue(sql.contains("'PRO001'")),
                () -> assertTrue(sql.endsWith(");"))
        );
    }

    @Test
    void deveGerarUpdateCorretamente() {

        CommandDTO command = buildCommand();

        String sql = OracleExecutionScriptBuilder.buildUpdate(
                DestinoImportacaoEnum.CONTA_CONTABIL_OPERADORA,
                command
        );

        assertAll("Deve gerar UPDATE corretamente",
                () -> assertTrue(sql.contains("UPDATE NEXTDIGITAL.CONTA_CONTABIL_OPERADORA")),
                () -> assertTrue(sql.contains("SET DESCRICAO_LEGADO")),
                () -> assertTrue(sql.contains("DESCRICAO_PROTHEUS")),
                () -> assertTrue(sql.contains("CODIGO_PROTHEUS")),
                () -> assertTrue(sql.contains("WHERE NOME_FILIAL = 'FILIAL01'")),
                () -> assertTrue(sql.contains("AND CODIGO_LEGADO = 'LEG001'")),
                () -> assertTrue(sql.contains("AND SISTEMA_LEGADO = 'PROTHEUS'")),
                () -> assertTrue(sql.endsWith(";"))
        );
    }

    @Test
    void deveGerarDeleteCorretamente() {

        CommandDTO command = buildCommand();

        String sql = OracleExecutionScriptBuilder.buildDelete(
                DestinoImportacaoEnum.CENTRO_CUSTO,
                command
        );

        assertAll("Deve gerar DELETE corretamente",
                () -> assertTrue(sql.contains("DELETE FROM NEXTDIGITAL.CENTRO_CUSTO")),
                () -> assertTrue(sql.contains("WHERE NOME_FILIAL = 'FILIAL01'")),
                () -> assertTrue(sql.contains("AND CODIGO_LEGADO = 'LEG001'")),
                () -> assertTrue(sql.contains("AND SISTEMA_LEGADO = 'PROTHEUS'")),
                () -> assertTrue(sql.endsWith(";"))
        );
    }

    @Test
    void deveEscaparAspasSimplesNoSql() {

        CommandDTO command = new CommandDTO(
                1,
                "FILIAL01",
                "Desc 'Legado'",
                "LEG'001",
                "protheus",
                "Desc 'Protheus'",
                "PRO'001"
        );

        String sql = OracleExecutionScriptBuilder.buildInsert(
                DestinoImportacaoEnum.CENTRO_CUSTO,
                command
        );

        assertAll("Deve escapar aspas simples corretamente",
                () -> assertTrue(sql.contains("Desc ''Legado''")),
                () -> assertTrue(sql.contains("LEG''001")),
                () -> assertTrue(sql.contains("Desc ''Protheus''")),
                () -> assertTrue(sql.contains("PRO''001"))
        );
    }

}
