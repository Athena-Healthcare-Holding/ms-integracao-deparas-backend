package io.platformbuilder.depara.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.platformbuilder.depara.dto.audit.DeParaExecutionAuditDTO;
import io.platformbuilder.depara.dto.audit.DeParaExecutionFilesDTO;
import io.platformbuilder.depara.dto.audit.DeParaExecutionItemDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AuditFileService {

    private static final Logger log = LoggerFactory.getLogger(AuditFileService.class);
    private static final DateTimeFormatter FOLDER_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    private final ObjectMapper objectMapper;
    private final String auditBasePath;

    public AuditFileService(
            @Value("${depara.auditoria.path:./src/main/resources/executados}") String auditBasePath) {
        this.auditBasePath = auditBasePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public DeParaExecutionFilesDTO saveExecutionFiles(DeParaExecutionAuditDTO auditDTO) {
        try {
            Path executionDirectory = createExecutionDirectory(auditDTO.getDestinoImportacao());

            String filePrefix = buildFilePrefix(auditDTO.getDestinoImportacao());

            Path jsonFilePath = executionDirectory.resolve(filePrefix + "-auditoria.json");
            Path rollbackSqlFilePath = executionDirectory.resolve(filePrefix + "-rollback.sql");
            Path insertSqlFilePath = executionDirectory.resolve(filePrefix + "-insert.sql");
            Path updateSqlFilePath = executionDirectory.resolve(filePrefix + "-update.sql");

            saveJsonFile(auditDTO, jsonFilePath);
            saveSqlRollbackFile(auditDTO, rollbackSqlFilePath);
            saveSqlInsertFile(auditDTO, insertSqlFilePath);
            saveSqlUpdateFile(auditDTO, updateSqlFilePath);

            log.info("Arquivos de auditoria salvos com sucesso. pasta={}, json={}, rollback={}, insert={}, update={}",
                    executionDirectory.toAbsolutePath(),
                    jsonFilePath.toAbsolutePath(),
                    rollbackSqlFilePath.toAbsolutePath(),
                    insertSqlFilePath.toAbsolutePath(),
                    updateSqlFilePath.toAbsolutePath());

            return new DeParaExecutionFilesDTO(
                    executionDirectory.toAbsolutePath().toString(),
                    jsonFilePath.toAbsolutePath().toString(),
                    rollbackSqlFilePath.toAbsolutePath().toString(),
                    insertSqlFilePath.toAbsolutePath().toString(),
                    updateSqlFilePath.toAbsolutePath().toString()
            );

        } catch (Exception e) {
            log.error("Erro ao salvar arquivos de auditoria da execução. motivo={}", e.getMessage(), e);
            throw new IllegalStateException("Não foi possível salvar os arquivos de auditoria da execução.", e);
        }
    }

    private Path createExecutionDirectory(DestinoImportacaoEnum destinoImportacao) throws Exception {
        Path baseDirectory = Paths.get(auditBasePath);
        Files.createDirectories(baseDirectory);

        String folderName = LocalDateTime.now().format(FOLDER_FORMATTER) + "-" + destinoImportacao.getPathValue();
        Path executionDirectory = baseDirectory.resolve(folderName);

        int suffix = 1;
        while (Files.exists(executionDirectory)) {
            executionDirectory = baseDirectory.resolve(folderName + "-" + suffix);
            suffix++;
        }

        Files.createDirectories(executionDirectory);
        return executionDirectory;
    }

    private String buildFilePrefix(DestinoImportacaoEnum destinoImportacao) {
        return "depara-" + destinoImportacao.getPathValue();
    }

    private void saveJsonFile(DeParaExecutionAuditDTO auditDTO, Path jsonFilePath) throws Exception {
        objectMapper.writeValue(new File(jsonFilePath.toString()), auditDTO);
    }

    private void saveSqlRollbackFile(DeParaExecutionAuditDTO auditDTO, Path sqlFilePath) throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(sqlFilePath, StandardCharsets.UTF_8);

        try {
            writer.write("-- Rollback gerado automaticamente pelo processo de de-para");
            writer.newLine();
            writeHeader(writer, auditDTO);

            for (DeParaExecutionItemDTO item : auditDTO.getRegistrosInseridos()) {
                writer.write(item.getDeleteOracle());
                writer.newLine();
            }

            if (!auditDTO.getRegistrosAtualizados().isEmpty()) {
                writer.newLine();
                writer.write("-- ATENCAO: houve registros atualizados.");
                writer.newLine();
                writer.write("-- O rollback real dos updates exige snapshot do valor anterior antes da atualização.");
                writer.newLine();
            }

            writer.newLine();
            writer.write("COMMIT;");
            writer.newLine();

        } finally {
            writer.close();
        }
    }

    private void saveSqlInsertFile(DeParaExecutionAuditDTO auditDTO, Path sqlFilePath) throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(sqlFilePath, StandardCharsets.UTF_8);

        try {
            writer.write("-- Inserts gerados automaticamente pelo processo de de-para");
            writer.newLine();
            writeHeader(writer, auditDTO);

            for (DeParaExecutionItemDTO item : auditDTO.getRegistrosInseridos()) {
                writer.write(item.getInsertOracle());
                writer.newLine();
            }

            writer.newLine();
            writer.write("COMMIT;");
            writer.newLine();

        } finally {
            writer.close();
        }
    }

    private void saveSqlUpdateFile(DeParaExecutionAuditDTO auditDTO, Path sqlFilePath) throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(sqlFilePath, StandardCharsets.UTF_8);

        try {
            writer.write("-- Updates gerados automaticamente pelo processo de de-para");
            writer.newLine();
            writeHeader(writer, auditDTO);

            for (DeParaExecutionItemDTO item : auditDTO.getRegistrosAtualizados()) {
                writer.write(item.getUpdateOracle());
                writer.newLine();
            }

            writer.newLine();
            writer.write("COMMIT;");
            writer.newLine();

        } finally {
            writer.close();
        }
    }

    private void writeHeader(BufferedWriter writer, DeParaExecutionAuditDTO auditDTO) throws Exception {
        writer.write("-- executionId: " + auditDTO.getExecutionId());
        writer.newLine();
        writer.write("-- destinoImportacao: " + auditDTO.getDestinoImportacao().name());
        writer.newLine();
        writer.write("-- dataHoraExecucao: " + auditDTO.getDataHoraExecucao());
        writer.newLine();
        writer.write("-- arquivoOrigem: " + auditDTO.getArquivoOrigem());
        writer.newLine();
        writer.newLine();
    }
}