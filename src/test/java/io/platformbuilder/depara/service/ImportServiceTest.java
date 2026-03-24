package io.platformbuilder.depara.service;

import io.platformbuilder.depara.dto.ImportResultDTO;
import io.platformbuilder.depara.dto.InsertDTO;
import io.platformbuilder.depara.dto.audit.DeParaExecutionAuditDTO;
import io.platformbuilder.depara.dto.audit.DeParaExecutionFilesDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import io.platformbuilder.depara.mapper.InsertMapper;
import io.platformbuilder.depara.repository.FilialJdbcRepository;
import io.platformbuilder.depara.repository.ImportJdbcRepository;
import io.platformbuilder.depara.repository.ImportJdbcRepositoryResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private ImportJdbcRepositoryResolver repositoryResolver;

    @Mock
    private FilialJdbcRepository filialJdbcRepository;

    @Mock
    private AuditFileService auditFileService;

    @Mock
    private InsertMapper parser;

    @Mock
    private ImportJdbcRepository repository;

    @Mock
    private MultipartFile arquivo;

    @InjectMocks
    private ImportService service;

    @BeforeEach
    void setUp() {
        when(arquivo.getOriginalFilename()).thenReturn("depara.xlsx");
    }

    @Test
    void deveImportarComSucessoSeparandoInsertEUpdate() {

        DestinoImportacaoEnum destino = DestinoImportacaoEnum.CENTRO_CUSTO;

        InsertDTO row1 = new InsertDTO(
                2,
                Collections.singletonList("02004015"),
                "MV",
                "PRO001",
                "LEG001",
                "Descricao 1"
        );

        InsertDTO row2 = new InsertDTO(
                3,
                Collections.singletonList("02004016"),
                "TASY",
                "PRO002",
                "LEG002",
                "Descricao 2"
        );

        when(repositoryResolver.resolve(destino)).thenReturn(repository);
        when(parser.parse(arquivo)).thenReturn(Arrays.asList(row1, row2));
        when(filialJdbcRepository.findExistingCodigosProtheus(anySet()))
                .thenReturn(new HashSet<>(Arrays.asList("02004015", "02004016")));
        when(repository.findExistingKeys(anyList()))
                .thenReturn(new HashSet<>(Collections.singletonList("02004015|LEG001")));
        when(repository.batchInsert(anyList())).thenReturn(new int[]{1});
        when(repository.batchUpdate(anyList())).thenReturn(new int[]{1});
        when(auditFileService.saveExecutionFiles(any(DeParaExecutionAuditDTO.class)))
                .thenReturn(new DeParaExecutionFilesDTO(
                        "/tmp/exec",
                        "/tmp/exec/audit.json",
                        "/tmp/exec/rollback.sql",
                        "/tmp/exec/insert.sql",
                        "/tmp/exec/update.sql"
                ));

        ImportResultDTO result = service.importar(arquivo, destino);

        assertAll(
                () -> assertEquals(2, result.getTotalRecebidos()),
                () -> assertEquals(1, result.getTotalInseridos()),
                () -> assertEquals(1, result.getTotalAtualizados()),
                () -> assertEquals(0, result.getTotalErros()),
                () -> assertEquals(1, result.getInseridos().size()),
                () -> assertEquals(1, result.getAtualizados().size()),
                () -> assertTrue(result.getErros().isEmpty())
        );

        verify(repository).batchInsert(argThat(list -> list.size() == 1
                && "02004016".equals(list.get(0).getNomeFilial())
                && "LEG002".equals(list.get(0).getCodigoLegado())));

        verify(repository).batchUpdate(argThat(list -> list.size() == 1
                && "02004015".equals(list.get(0).getNomeFilial())
                && "LEG001".equals(list.get(0).getCodigoLegado())));

        verify(auditFileService).saveExecutionFiles(any(DeParaExecutionAuditDTO.class));
    }

    @Test
    void deveRetornarErroQuandoSistemaLegadoForInvalido() {
        DestinoImportacaoEnum destino = DestinoImportacaoEnum.CENTRO_CUSTO;

        InsertDTO row = new InsertDTO(
                2,
                Collections.singletonList("02004015"),
                "SAP",
                "PRO001",
                "LEG001",
                "Descricao 1"
        );

        when(repositoryResolver.resolve(destino)).thenReturn(repository);
        when(parser.parse(arquivo)).thenReturn(Collections.singletonList(row));
        when(filialJdbcRepository.findExistingCodigosProtheus(anySet()))
                .thenReturn(new HashSet<>(Collections.singletonList("02004015")));
        when(repository.findExistingKeys(anyList())).thenReturn(Collections.emptySet());
        when(auditFileService.saveExecutionFiles(any(DeParaExecutionAuditDTO.class)))
                .thenReturn(new DeParaExecutionFilesDTO(
                        "/tmp/exec",
                        "/tmp/exec/audit.json",
                        "/tmp/exec/rollback.sql",
                        "/tmp/exec/insert.sql",
                        "/tmp/exec/update.sql"
                ));

        ImportResultDTO result = service.importar(arquivo, destino);

        assertAll(
                () -> assertEquals(1, result.getTotalRecebidos()),
                () -> assertEquals(0, result.getTotalInseridos()),
                () -> assertEquals(0, result.getTotalAtualizados()),
                () -> assertEquals(1, result.getTotalErros()),
                () -> assertEquals("Sistema legado inválido. Valores permitidos: SOLUS, MV, TASY.",
                        result.getErros().get(0).getMensagem())
        );

        verify(repository, never()).batchInsert(anyList());
        verify(repository, never()).batchUpdate(anyList());
    }

    @Test
    void deveRetornarErroQuandoFilialNaoExistir() {
        DestinoImportacaoEnum destino = DestinoImportacaoEnum.CENTRO_CUSTO;

        InsertDTO row = new InsertDTO(
                2,
                Collections.singletonList("99999999"),
                "MV",
                "PRO001",
                "LEG001",
                "Descricao 1"
        );

        when(repositoryResolver.resolve(destino)).thenReturn(repository);
        when(parser.parse(arquivo)).thenReturn(Collections.singletonList(row));
        when(filialJdbcRepository.findExistingCodigosProtheus(anySet()))
                .thenReturn(Collections.emptySet());
        when(repository.findExistingKeys(anyList())).thenReturn(Collections.emptySet());
        when(auditFileService.saveExecutionFiles(any(DeParaExecutionAuditDTO.class)))
                .thenReturn(new DeParaExecutionFilesDTO(
                        "/tmp/exec",
                        "/tmp/exec/audit.json",
                        "/tmp/exec/rollback.sql",
                        "/tmp/exec/insert.sql",
                        "/tmp/exec/update.sql"
                ));

        ImportResultDTO result = service.importar(arquivo, destino);

        assertAll(
                () -> assertEquals(1, result.getTotalRecebidos()),
                () -> assertEquals(0, result.getTotalInseridos()),
                () -> assertEquals(0, result.getTotalAtualizados()),
                () -> assertEquals(1, result.getTotalErros()),
                () -> assertEquals("Filial não encontrada na tabela FILIAL para o código informado.",
                        result.getErros().get(0).getMensagem())
        );

        verify(repository, never()).batchInsert(anyList());
        verify(repository, never()).batchUpdate(anyList());
    }

    @Test
    void deveSalvarAuditoriaComDadosDoResultado() {
        DestinoImportacaoEnum destino = DestinoImportacaoEnum.CENTRO_CUSTO;

        InsertDTO row = new InsertDTO(
                2,
                Collections.singletonList("02004015"),
                "MV",
                "PRO001",
                "LEG001",
                "Descricao 1"
        );

        when(repositoryResolver.resolve(destino)).thenReturn(repository);
        when(parser.parse(arquivo)).thenReturn(Collections.singletonList(row));
        when(filialJdbcRepository.findExistingCodigosProtheus(anySet()))
                .thenReturn(new HashSet<>(Collections.singletonList("02004015")));
        when(repository.findExistingKeys(anyList())).thenReturn(Collections.emptySet());
        when(repository.batchInsert(anyList())).thenReturn(new int[]{1});
        when(auditFileService.saveExecutionFiles(any(DeParaExecutionAuditDTO.class)))
                .thenReturn(new DeParaExecutionFilesDTO(
                        "/tmp/exec",
                        "/tmp/exec/audit.json",
                        "/tmp/exec/rollback.sql",
                        "/tmp/exec/insert.sql",
                        "/tmp/exec/update.sql"
                ));

        service.importar(arquivo, destino);

        ArgumentCaptor<DeParaExecutionAuditDTO> auditCaptor =
                ArgumentCaptor.forClass(DeParaExecutionAuditDTO.class);

        verify(auditFileService).saveExecutionFiles(auditCaptor.capture());

        DeParaExecutionAuditDTO audit = auditCaptor.getValue();

        assertAll(
                () -> assertNotNull(audit.getExecutionId()),
                () -> assertEquals(destino, audit.getDestinoImportacao()),
                () -> assertEquals("depara.xlsx", audit.getArquivoOrigem()),
                () -> assertEquals(1, audit.getTotalRecebidos()),
                () -> assertEquals(1, audit.getTotalInseridos()),
                () -> assertEquals(0, audit.getTotalAtualizados()),
                () -> assertEquals(0, audit.getTotalErros()),
                () -> assertEquals(1, audit.getRegistrosInseridos().size()),
                () -> assertTrue(audit.getRegistrosAtualizados().isEmpty())
        );
    }
}
