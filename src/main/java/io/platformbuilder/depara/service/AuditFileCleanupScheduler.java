package io.platformbuilder.depara.service;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
public class AuditFileCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuditFileCleanupScheduler.class);

    private final String auditBasePath;
    private final long retentionDays;

    public AuditFileCleanupScheduler(
            @Value("${depara.auditoria.path:./data/depara/executados}") String auditBasePath,
            @Value("${depara.auditoria.cleanup.retention-days:5}") long retentionDays) {
        this.auditBasePath = auditBasePath;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "${depara.auditoria.cleanup.cron:0 0 0 * * *}")
    public void deleteExpiredAuditFiles() {

        Path baseDirectory = Paths.get(auditBasePath);

        if (!Files.exists(baseDirectory)) {
            log.info("Limpeza de auditoria ignorada. Diretório base não existe: {}", baseDirectory.toAbsolutePath());
            return;
        }

        if (!Files.isDirectory(baseDirectory)) {
            log.warn("Limpeza de auditoria ignorada. Caminho configurado não é um diretório: {}", baseDirectory.toAbsolutePath());
            return;
        }

        LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(retentionDays);

        log.info("Iniciando limpeza de arquivos de auditoria. pastaBase={}, retentionDays={}, removendo itens anteriores a={}",
                baseDirectory.toAbsolutePath(),
                retentionDays,
                cutoffDateTime);

        CleanupSummary summary = new CleanupSummary();

        try {
            Files.list(baseDirectory).forEach(path -> processPath(path, cutoffDateTime, summary));
        } catch (IOException e) {
            log.error("Erro ao listar conteúdo da pasta de auditoria: {}", baseDirectory.toAbsolutePath(), e);
        }

        log.info("Limpeza finalizada. pastasRemovidas={}, arquivosRemovidos={}, falhas={}",
                summary.getDeletedDirectories(),
                summary.getDeletedFiles(),
                summary.getFailures());

    }

    private void processPath(Path path, LocalDateTime cutoffDateTime, CleanupSummary summary) {

        try {

            LocalDateTime lastModified = getLastModifiedDateTime(path);

            if (lastModified.isBefore(cutoffDateTime)) {

                if (Files.isDirectory(path)) {
                    deleteDirectoryRecursively(path, summary);
                    summary.incrementDeletedDirectories();
                    log.info("Diretório removido com sucesso: {}", path.toAbsolutePath());
                } else {
                    Files.deleteIfExists(path);
                    summary.incrementDeletedFiles();
                    log.info("Arquivo removido com sucesso: {}", path.toAbsolutePath());
                }

            }

        } catch (Exception e) {
            summary.incrementFailures();
            log.error("Erro ao processar item para limpeza: {}", path.toAbsolutePath(), e);
        }

    }

    private LocalDateTime getLastModifiedDateTime(Path path) throws IOException {
        Instant instant = Files.getLastModifiedTime(path).toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
    }

    private void deleteDirectoryRecursively(Path directory, CleanupSummary summary) throws IOException {

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                summary.incrementDeletedFiles();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }

                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });

    }

    @Getter
    @Setter
    private static final class CleanupSummary {

        private int deletedDirectories;
        private int deletedFiles;
        private int failures;

        public void incrementDeletedDirectories() {
            this.deletedDirectories++;
        }

        public void incrementDeletedFiles() {
            this.deletedFiles++;
        }

        public void incrementFailures() {
            this.failures++;
        }

    }

}
