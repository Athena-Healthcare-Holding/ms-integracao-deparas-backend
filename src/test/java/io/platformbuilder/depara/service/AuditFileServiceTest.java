package io.platformbuilder.depara.service;

import io.platformbuilder.depara.dto.audit.DeParaExecutionAuditDTO;
import io.platformbuilder.depara.dto.audit.DeParaExecutionFilesDTO;
import io.platformbuilder.depara.dto.audit.DeParaExecutionItemDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditFileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void deveSalvarArquivosDeAuditoriaCorretamente() throws Exception {

        AuditFileService service = new AuditFileService(tempDir.toString());

        DeParaExecutionItemDTO itemInserido = new DeParaExecutionItemDTO(
                2,
                "02004015",
                "LEG001",
                "PRO001",
                "INSERT INTO NEXTDIGITAL.CENTRO_CUSTO VALUES ('PRO001');",
                "UPDATE NEXTDIGITAL.CENTRO_CUSTO SET CODIGO_PROTHEUS = 'PRO001';",
                "DELETE FROM NEXTDIGITAL.CENTRO_CUSTO WHERE CODIGO_LEGADO = 'LEG001';"
        );

        DeParaExecutionItemDTO itemAtualizado = new DeParaExecutionItemDTO(
                3,
                "02004016",
                "LEG002",
                "PRO002",
                "INSERT INTO NEXTDIGITAL.CENTRO_CUSTO VALUES ('PRO002');",
                "UPDATE NEXTDIGITAL.CENTRO_CUSTO SET CODIGO_PROTHEUS = 'PRO002';",
                "DELETE FROM NEXTDIGITAL.CENTRO_CUSTO WHERE CODIGO_LEGADO = 'LEG002';"
        );

        DeParaExecutionAuditDTO auditDTO = new DeParaExecutionAuditDTO(
                "EXEC-123",
                DestinoImportacaoEnum.CENTRO_CUSTO,
                "2026-03-19T15:30:00",
                "arquivo.xlsx",
                2,
                1,
                1,
                0,
                Collections.singletonList(itemInserido),
                Collections.singletonList(itemAtualizado)
        );

        DeParaExecutionFilesDTO result = service.saveExecutionFiles(auditDTO);

        assertAll(
                () -> assertNotNull(result),
                () -> assertNotNull(result.getExecutionFolder()),
                () -> assertNotNull(result.getJsonFilePath()),
                () -> assertNotNull(result.getRollbackSqlFilePath()),
                () -> assertNotNull(result.getInsertSqlFilePath()),
                () -> assertNotNull(result.getUpdateSqlFilePath())
        );

        Path executionFolder = Paths.get(result.getExecutionFolder());
        Path jsonPath = Paths.get(result.getJsonFilePath());
        Path rollbackPath = Paths.get(result.getRollbackSqlFilePath());
        Path insertPath = Paths.get(result.getInsertSqlFilePath());
        Path updatePath = Paths.get(result.getUpdateSqlFilePath());

        assertAll(
                () -> assertTrue(Files.exists(executionFolder)),
                () -> assertTrue(Files.isDirectory(executionFolder)),
                () -> assertTrue(Files.exists(jsonPath)),
                () -> assertTrue(Files.exists(rollbackPath)),
                () -> assertTrue(Files.exists(insertPath)),
                () -> assertTrue(Files.exists(updatePath))
        );

        String jsonContent = new String(Files.readAllBytes(jsonPath), StandardCharsets.UTF_8);
        String rollbackContent = new String(Files.readAllBytes(rollbackPath), StandardCharsets.UTF_8);
        String insertContent = new String(Files.readAllBytes(insertPath), StandardCharsets.UTF_8);
        String updateContent = new String(Files.readAllBytes(updatePath), StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(jsonContent.contains("\"executionId\" : \"EXEC-123\"")),
                () -> assertTrue(jsonContent.contains("\"arquivoOrigem\" : \"arquivo.xlsx\"")),
                () -> assertTrue(jsonContent.contains("\"totalInseridos\" : 1")),
                () -> assertTrue(jsonContent.contains("\"totalAtualizados\" : 1")),

                () -> assertTrue(rollbackContent.contains("-- Rollback gerado automaticamente pelo processo de de-para")),
                () -> assertTrue(rollbackContent.contains("-- executionId: EXEC-123")),
                () -> assertTrue(rollbackContent.contains("DELETE FROM NEXTDIGITAL.CENTRO_CUSTO WHERE CODIGO_LEGADO = 'LEG001';")),
                () -> assertTrue(rollbackContent.contains("-- ATENCAO: houve registros atualizados.")),
                () -> assertTrue(rollbackContent.contains("COMMIT;")),

                () -> assertTrue(insertContent.contains("-- Inserts gerados automaticamente pelo processo de de-para")),
                () -> assertTrue(insertContent.contains("-- arquivoOrigem: arquivo.xlsx")),
                () -> assertTrue(insertContent.contains("INSERT INTO NEXTDIGITAL.CENTRO_CUSTO VALUES ('PRO001');")),
                () -> assertTrue(insertContent.contains("COMMIT;")),

                () -> assertTrue(updateContent.contains("-- Updates gerados automaticamente pelo processo de de-para")),
                () -> assertTrue(updateContent.contains("-- destinoImportacao: CENTRO_CUSTO")),
                () -> assertTrue(updateContent.contains("UPDATE NEXTDIGITAL.CENTRO_CUSTO SET CODIGO_PROTHEUS = 'PRO002';")),
                () -> assertTrue(updateContent.contains("COMMIT;"))
        );

    }

    @Test
    void deveCriarPastaComSufixoQuandoJaExistirUmaPastaComMesmoNomeBase() throws Exception {

        AuditFileService service = new AuditFileService(tempDir.toString());

        DeParaExecutionAuditDTO auditDTO = new DeParaExecutionAuditDTO(
                "EXEC-456",
                DestinoImportacaoEnum.CENTRO_CUSTO,
                "2026-03-19T16:00:00",
                "arquivo.xlsx",
                1,
                0,
                0,
                1,
                Collections.emptyList(),
                Collections.emptyList()
        );

        DeParaExecutionFilesDTO first = service.saveExecutionFiles(auditDTO);
        DeParaExecutionFilesDTO second = service.saveExecutionFiles(auditDTO);

        assertAll(
                () -> assertNotEquals(first.getExecutionFolder(), second.getExecutionFolder()),
                () -> assertTrue(Files.exists(Paths.get(first.getExecutionFolder()))),
                () -> assertTrue(Files.exists(Paths.get(second.getExecutionFolder())))
        );

    }

    @Test
    void deveGerarArquivosSqlMesmoSemRegistrosInseridosOuAtualizados() throws Exception {

        AuditFileService service = new AuditFileService(tempDir.toString());

        DeParaExecutionAuditDTO auditDTO = new DeParaExecutionAuditDTO(
                "EXEC-789",
                DestinoImportacaoEnum.CONTA_CONTABIL_OPERADORA,
                "2026-03-19T17:00:00",
                "vazio.xlsx",
                0,
                0,
                0,
                0,
                Collections.emptyList(),
                Collections.emptyList()
        );

        DeParaExecutionFilesDTO result = service.saveExecutionFiles(auditDTO);

        String rollbackContent = new String(Files.readAllBytes(Paths.get(result.getRollbackSqlFilePath())), StandardCharsets.UTF_8);
        String insertContent = new String(Files.readAllBytes(Paths.get(result.getInsertSqlFilePath())), StandardCharsets.UTF_8);
        String updateContent = new String(Files.readAllBytes(Paths.get(result.getUpdateSqlFilePath())), StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(rollbackContent.contains("COMMIT;")),
                () -> assertTrue(insertContent.contains("COMMIT;")),
                () -> assertTrue(updateContent.contains("COMMIT;")),
                () -> assertTrue(insertContent.contains("-- destinoImportacao: CONTA_CONTABIL_OPERADORA")),
                () -> assertTrue(updateContent.contains("-- arquivoOrigem: vazio.xlsx"))
        );

    }

}
