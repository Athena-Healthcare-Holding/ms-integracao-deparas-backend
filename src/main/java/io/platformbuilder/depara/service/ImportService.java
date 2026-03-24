package io.platformbuilder.depara.service;

import io.platformbuilder.depara.dto.CommandDTO;
import io.platformbuilder.depara.dto.ImportResultDTO;
import io.platformbuilder.depara.dto.InsertDTO;
import io.platformbuilder.depara.dto.ItemResultDTO;
import io.platformbuilder.depara.dto.audit.DeParaExecutionAuditDTO;
import io.platformbuilder.depara.dto.audit.DeParaExecutionFilesDTO;
import io.platformbuilder.depara.dto.audit.DeParaExecutionItemDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import io.platformbuilder.depara.enums.StatusEnum;
import io.platformbuilder.depara.mapper.InsertMapper;
import io.platformbuilder.depara.repository.FilialJdbcRepository;
import io.platformbuilder.depara.repository.ImportJdbcRepository;
import io.platformbuilder.depara.repository.ImportJdbcRepositoryResolver;
import io.platformbuilder.depara.util.OracleExecutionScriptBuilder;
import io.platformbuilder.depara.util.SistemaLegadoValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final ImportJdbcRepositoryResolver repositoryResolver;
    private final FilialJdbcRepository filialJdbcRepository;
    private final AuditFileService auditFileService;
    private final InsertMapper parser;

    public ImportService(ImportJdbcRepositoryResolver repositoryResolver,
                         FilialJdbcRepository filialJdbcRepository,
                         AuditFileService auditFileService,
                         InsertMapper parser) {
        this.filialJdbcRepository = filialJdbcRepository;
        this.repositoryResolver = repositoryResolver;
        this.auditFileService = auditFileService;
        this.parser = parser;
    }

    public ImportResultDTO importar(MultipartFile arquivo, DestinoImportacaoEnum destino) {

        log.info("Iniciando importação. arquivo={}, destino={}", arquivo.getOriginalFilename(), destino);

        ImportJdbcRepository repository = repositoryResolver.resolve(destino);

        List<InsertDTO> rows = parser.parse(arquivo);
        List<CommandDTO> commands = InsertMapper.toCommands(rows);

        List<ItemResultDTO> inseridos = new ArrayList<>();
        List<ItemResultDTO> atualizados = new ArrayList<>();
        List<ItemResultDTO> erros = new ArrayList<>();

        List<CommandDTO> comandosInseridosComSucesso = new ArrayList<>();
        List<CommandDTO> comandosAtualizadosComSucesso = new ArrayList<>();

        Set<String> filiaisInformadas = extractFiliais(commands);
        Set<String> filiaisExistentes = filialJdbcRepository.findExistingCodigosProtheus(filiaisInformadas);

        List<CommandDTO> commandsComFilialValida = new ArrayList<>();

        for (CommandDTO command : commands) {

            if (!SistemaLegadoValidator.isValido(command.getSistemaLegado())) {

                erros.add(new ItemResultDTO(
                        command.getLinhaOrigemExcel(),
                        command.getNomeFilial(),
                        command.getCodigoLegado(),
                        command.getCodigoProtheus(),
                        StatusEnum.ERRO,
                        "Sistema legado inválido. Valores permitidos: SOLUS, MV, TASY."
                ));

                log.warn("Registro rejeitado por sistema legado inválido. destino={}, linha={}, sistema={}, filial={}, codigoLegado={}",
                        destino,
                        command.getLinhaOrigemExcel(),
                        command.getSistemaLegado(),
                        command.getNomeFilial(),
                        command.getCodigoLegado());

                continue;
            }

            command.setSistemaLegado(SistemaLegadoValidator.normalizar(command.getSistemaLegado()));

            if (!filiaisExistentes.contains(command.getNomeFilial())) {

                erros.add(new ItemResultDTO(
                        command.getLinhaOrigemExcel(),
                        command.getNomeFilial(),
                        command.getCodigoLegado(),
                        command.getCodigoProtheus(),
                        StatusEnum.ERRO,
                        "Filial não encontrada na tabela FILIAL para o código informado."
                ));

                log.warn("Registro rejeitado por filial inexistente. destino={}, linha={}, filial={}, codigoLegado={}",
                        destino,
                        command.getLinhaOrigemExcel(),
                        command.getNomeFilial(),
                        command.getCodigoLegado());

                continue;
            }

            commandsComFilialValida.add(command);
        }

        Set<String> existingKeys = repository.findExistingKeys(commandsComFilialValida);

        List<CommandDTO> pendentesInsert = new ArrayList<>();
        List<CommandDTO> pendentesUpdate = new ArrayList<>();

        for (CommandDTO command : commandsComFilialValida) {
            if (existingKeys.contains(command.uniqueKey())) {
                pendentesUpdate.add(command);
            } else {
                pendentesInsert.add(command);
            }
        }

        processarInserts(destino, repository, pendentesInsert, inseridos, erros, comandosInseridosComSucesso);
        processarUpdates(destino, repository, pendentesUpdate, atualizados, erros, comandosAtualizadosComSucesso);

        ImportResultDTO resultado = new ImportResultDTO(atualizados, inseridos, erros, commands.size());

        salvarAuditoria(destino, arquivo, resultado, comandosInseridosComSucesso, comandosAtualizadosComSucesso);

        log.info("Importação finalizada. destino={}, totalRecebidos={}, inseridos={}, atualizados={}, erros={}",
                destino,
                resultado.getTotalRecebidos(),
                resultado.getTotalInseridos(),
                resultado.getTotalAtualizados(),
                resultado.getTotalErros());

        return resultado;
    }

    private void processarInserts(DestinoImportacaoEnum destino,
                                  ImportJdbcRepository repository,
                                  List<CommandDTO> pendentesInsert,
                                  List<ItemResultDTO> inseridos,
                                  List<ItemResultDTO> erros,
                                  List<CommandDTO> comandosInseridosComSucesso) {

        if (pendentesInsert.isEmpty()) {
            return;
        }

        try {

            log.info("Iniciando batch insert. destino={}, quantidade={}", destino, pendentesInsert.size());
            repository.batchInsert(pendentesInsert);

            for (CommandDTO command : pendentesInsert) {
                inseridos.add(buildItemResult(command, StatusEnum.INSERIDO, "Registro inserido com sucesso"));
                comandosInseridosComSucesso.add(command);
            }

            log.info("Batch insert concluído com sucesso. destino={}, quantidade={}", destino, pendentesInsert.size());

        } catch (Exception e) {
            log.error("Falha no batch insert. destino={}, motivo={}", destino, e.getMessage(), e);
            processarFallbackInsert(destino, repository, pendentesInsert, inseridos, erros, comandosInseridosComSucesso);
        }

    }

    private void processarUpdates(DestinoImportacaoEnum destino,
                                  ImportJdbcRepository repository,
                                  List<CommandDTO> pendentesUpdate,
                                  List<ItemResultDTO> atualizados,
                                  List<ItemResultDTO> erros,
                                  List<CommandDTO> comandosAtualizadosComSucesso) {

        if (pendentesUpdate.isEmpty()) {
            return;
        }

        try {

            log.info("Iniciando batch update. destino={}, quantidade={}", destino, pendentesUpdate.size());
            repository.batchUpdate(pendentesUpdate);

            for (CommandDTO command : pendentesUpdate) {
                atualizados.add(buildItemResult(command, StatusEnum.ATUALIZADO, "Registro atualizado com sucesso"));
                comandosAtualizadosComSucesso.add(command);
            }

            log.info("Batch update concluído com sucesso. destino={}, quantidade={}", destino, pendentesUpdate.size());

        } catch (Exception e) {
            log.error("Falha no batch update. destino={}, motivo={}", destino, e.getMessage(), e);
            processarFallbackUpdate(destino, repository, pendentesUpdate, atualizados, erros, comandosAtualizadosComSucesso);
        }

    }

    private void processarFallbackInsert(DestinoImportacaoEnum destino,
                                         ImportJdbcRepository repository,
                                         List<CommandDTO> pendentesInsert,
                                         List<ItemResultDTO> inseridos,
                                         List<ItemResultDTO> erros,
                                         List<CommandDTO> comandosInseridosComSucesso) {

        for (CommandDTO command : pendentesInsert) {

            try {

                repository.insert(command);
                inseridos.add(buildItemResult(command, StatusEnum.INSERIDO, "Registro inserido com sucesso"));
                comandosInseridosComSucesso.add(command);

                log.info("Insert individual realizado com sucesso. destino={}, linha={}, filial={}, codigoLegado={}",
                        destino, command.getLinhaOrigemExcel(), command.getNomeFilial(), command.getCodigoLegado());

            } catch (Exception ex) {

                erros.add(buildItemResult(command, StatusEnum.ERRO, ex.getMessage()));
                log.error("Erro no insert individual. destino={}, linha={}, filial={}, codigoLegado={}, motivo={}",
                        destino,
                        command.getLinhaOrigemExcel(),
                        command.getNomeFilial(),
                        command.getCodigoLegado(),
                        ex.getMessage(),
                        ex);

            }
        }

    }

    private void processarFallbackUpdate(DestinoImportacaoEnum destino,
                                         ImportJdbcRepository repository,
                                         List<CommandDTO> pendentesUpdate,
                                         List<ItemResultDTO> atualizados,
                                         List<ItemResultDTO> erros,
                                         List<CommandDTO> comandosAtualizadosComSucesso) {

        for (CommandDTO command : pendentesUpdate) {
            try {

                repository.update(command);
                atualizados.add(buildItemResult(command, StatusEnum.ATUALIZADO, "Registro atualizado com sucesso"));
                comandosAtualizadosComSucesso.add(command);

                log.info("Update individual realizado com sucesso. destino={}, linha={}, filial={}, codigoLegado={}",
                        destino, command.getLinhaOrigemExcel(), command.getNomeFilial(), command.getCodigoLegado());

            } catch (Exception ex) {
                erros.add(buildItemResult(command, StatusEnum.ERRO, ex.getMessage()));

                log.error("Erro no update individual. destino={}, linha={}, filial={}, codigoLegado={}, motivo={}",
                        destino,
                        command.getLinhaOrigemExcel(),
                        command.getNomeFilial(),
                        command.getCodigoLegado(),
                        ex.getMessage(),
                        ex);
            }
        }
    }

    private ItemResultDTO buildItemResult(CommandDTO command,
                                          StatusEnum status,
                                          String mensagem) {
        return new ItemResultDTO(
                command.getLinhaOrigemExcel(),
                command.getNomeFilial(),
                command.getCodigoLegado(),
                command.getCodigoProtheus(),
                status,
                mensagem
        );
    }

    private Set<String> extractFiliais(List<CommandDTO> commands) {

        Set<String> filiais = new HashSet<>();

        for (CommandDTO command : commands) {
            if (command.getNomeFilial() != null && !command.getNomeFilial().trim().isEmpty()) {
                filiais.add(command.getNomeFilial().trim());
            }
        }

        return filiais;
    }

    private void salvarAuditoria(DestinoImportacaoEnum destino,
                                 MultipartFile arquivo,
                                 ImportResultDTO resultado,
                                 List<CommandDTO> comandosInseridosComSucesso,
                                 List<CommandDTO> comandosAtualizadosComSucesso) {
        try {

            String executionId = generateExecutionId();
            String dataHoraExecucao = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            List<DeParaExecutionItemDTO> itensInseridos = buildExecutionItems(comandosInseridosComSucesso, destino);
            List<DeParaExecutionItemDTO> itensAtualizados = buildExecutionItems(comandosAtualizadosComSucesso, destino);

            DeParaExecutionAuditDTO auditDTO = new DeParaExecutionAuditDTO(
                    executionId,
                    destino,
                    dataHoraExecucao,
                    arquivo.getOriginalFilename(),
                    resultado.getTotalRecebidos(),
                    resultado.getTotalInseridos(),
                    resultado.getTotalAtualizados(),
                    resultado.getTotalErros(),
                    itensInseridos,
                    itensAtualizados
            );

            DeParaExecutionFilesDTO filesDTO = auditFileService.saveExecutionFiles(auditDTO);

            log.info("Arquivos de auditoria gerados com sucesso. pasta={}, json={}, rollback={}, insert={}, update={}",
                    filesDTO.getExecutionFolder(),
                    filesDTO.getJsonFilePath(),
                    filesDTO.getRollbackSqlFilePath(),
                    filesDTO.getInsertSqlFilePath(),
                    filesDTO.getUpdateSqlFilePath());

        } catch (Exception e) {
            log.error("Erro ao salvar auditoria da execução. motivo={}", e.getMessage(), e);
        }

    }

    private List<DeParaExecutionItemDTO> buildExecutionItems(List<CommandDTO> commands, DestinoImportacaoEnum destino) {

        List<DeParaExecutionItemDTO> items = new ArrayList<>();

        for (CommandDTO command : commands) {
            items.add(new DeParaExecutionItemDTO(
                    command.getLinhaOrigemExcel(),
                    command.getNomeFilial(),
                    command.getCodigoLegado(),
                    command.getCodigoProtheus(),
                    OracleExecutionScriptBuilder.buildInsert(destino, command),
                    OracleExecutionScriptBuilder.buildUpdate(destino, command),
                    OracleExecutionScriptBuilder.buildDelete(destino, command)
            ));
        }

        return items;
    }

    private String generateExecutionId() {
        return "EXEC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

}