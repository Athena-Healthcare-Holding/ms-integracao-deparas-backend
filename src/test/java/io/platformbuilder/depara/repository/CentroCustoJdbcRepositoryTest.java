package io.platformbuilder.depara.repository;

import io.platformbuilder.depara.dto.CommandDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import io.platformbuilder.depara.sql.SqlStatements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CentroCustoJdbcRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PreparedStatement preparedStatement;

    private CentroCustoJdbcRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CentroCustoJdbcRepository(jdbcTemplate);
    }

    @Test
    void deveRetornarDestinoCentroCusto() {
        assertEquals(DestinoImportacaoEnum.CENTRO_CUSTO, repository.getDestino());
    }

    @Test
    void deveRetornarSetVazioQuandoFindExistingKeysReceberListaVazia() {
        Set<String> result = repository.findExistingKeys(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void deveConsultarFindExistingKeysERetornarSetSemDuplicidade() {

        List<CommandDTO> commands = Arrays.asList(
                buildCommand("FILIAL01", "LEG001", "mv"),
                buildCommand("FILIAL02", "LEG002", "tasy")
        );

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(Arrays.asList("FILIAL01|LEG001", "FILIAL01|LEG001", "FILIAL02|LEG002"));

        Set<String> result = repository.findExistingKeys(commands);

        assertEquals(2, result.size());
        assertTrue(result.contains("FILIAL01|LEG001"));
        assertTrue(result.contains("FILIAL02|LEG002"));

    }

    @Test
    void deveMontarSqlEParametrosCorretamenteNoFindExistingKeys() {

        List<CommandDTO> commands = Arrays.asList(
                buildCommand("FILIAL01", "LEG001", "mv"),
                buildCommand("FILIAL02", "LEG002", "tasy")
        );

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);

        repository.findExistingKeys(commands);

        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(), any(RowMapper.class));

        String sql = sqlCaptor.getValue();
        Object[] params = paramsCaptor.getValue();
        List<Object> paramsList = Arrays.asList(params);

        assertAll(
                () -> assertNotNull(sql),
                () -> assertTrue(sql.contains("?")),
                () -> assertEquals(6, params.length),
                () -> assertTrue(paramsList.contains("MV")),
                () -> assertTrue(paramsList.contains("TASY")),
                () -> assertTrue(paramsList.contains("FILIAL01")),
                () -> assertTrue(paramsList.contains("FILIAL02")),
                () -> assertTrue(paramsList.contains("LEG001")),
                () -> assertTrue(paramsList.contains("LEG002"))
        );

    }

    @Test
    void deveRetornarArrayVazioQuandoBatchInsertReceberListaNulaOuVazia() {
        assertEquals(0, repository.batchInsert(null).length);
        assertEquals(0, repository.batchInsert(Collections.emptyList()).length);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void deveChamarBatchInsertComSqlCorretoESetterConfigurado() throws SQLException {

        List<CommandDTO> commands = Arrays.asList(
                buildCommand("FILIAL01", "LEG001", "MV"),
                buildCommand("FILIAL02", "LEG002", "TASY")
        );

        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1, 1});

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BatchPreparedStatementSetter> setterCaptor =
                ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);

        int[] result = repository.batchInsert(commands);

        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), setterCaptor.capture());

        BatchPreparedStatementSetter setter = setterCaptor.getValue();

        assertAll(
                () -> assertArrayEquals(new int[]{1, 1}, result),
                () -> assertEquals(SqlStatements.INSERT_CENTRO_CUSTO, sqlCaptor.getValue()),
                () -> assertEquals(2, setter.getBatchSize())
        );

        setter.setValues(preparedStatement, 0);

        verify(preparedStatement).setString(1, "FILIAL01");
        verify(preparedStatement).setString(2, "Descricao Legado");
        verify(preparedStatement).setString(3, "LEG001");
        verify(preparedStatement).setString(4, "MV");
        verify(preparedStatement).setString(5, "Descricao Protheus");
        verify(preparedStatement).setString(6, "PRO001");

    }

    @Test
    void deveRetornarArrayVazioQuandoBatchUpdateReceberListaNulaOuVazia() {
        assertEquals(0, repository.batchUpdate(null).length);
        assertEquals(0, repository.batchUpdate(Collections.emptyList()).length);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void deveChamarBatchUpdateComSqlCorretoESetterConfigurado() throws SQLException {

        List<CommandDTO> commands = Collections.singletonList(
                buildCommand("FILIAL01", "LEG001", "MV")
        );

        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1});

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BatchPreparedStatementSetter> setterCaptor =
                ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);

        int[] result = repository.batchUpdate(commands);

        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), setterCaptor.capture());

        BatchPreparedStatementSetter setter = setterCaptor.getValue();

        assertAll(
                () -> assertArrayEquals(new int[]{1}, result),
                () -> assertEquals(SqlStatements.UPDATE_CENTRO_CUSTO, sqlCaptor.getValue()),
                () -> assertEquals(1, setter.getBatchSize())
        );

        setter.setValues(preparedStatement, 0);

        verify(preparedStatement).setString(1, "Descricao Legado");
        verify(preparedStatement).setString(2, "Descricao Protheus");
        verify(preparedStatement).setString(3, "PRO001");
        verify(preparedStatement).setString(4, "FILIAL01");
        verify(preparedStatement).setString(5, "LEG001");
        verify(preparedStatement).setString(6, "MV");

    }

    @Test
    void deveLancarExcecaoQuandoInsertReceberCommandNulo() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.insert(null)
        );

        assertEquals("Command para insert não pode ser nulo.", exception.getMessage());
        verifyNoInteractions(jdbcTemplate);

    }

    @Test
    void deveExecutarInsertIndividualCorretamente() {

        CommandDTO command = buildCommand("FILIAL01", "LEG001", "MV");

        when(jdbcTemplate.update(
                eq(SqlStatements.INSERT_CENTRO_CUSTO),
                any(), any(), any(), any(), any(), any()
        )).thenReturn(1);

        int result = repository.insert(command);

        assertEquals(1, result);

        verify(jdbcTemplate).update(
                SqlStatements.INSERT_CENTRO_CUSTO,
                "FILIAL01",
                "Descricao Legado",
                "LEG001",
                "MV",
                "Descricao Protheus",
                "PRO001"
        );

    }

    @Test
    void deveLancarExcecaoQuandoUpdateReceberCommandNulo() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> repository.update(null)
        );

        assertEquals("Command para update não pode ser nulo.", exception.getMessage());
        verifyNoInteractions(jdbcTemplate);

    }

    @Test
    void deveExecutarUpdateIndividualCorretamente() {

        CommandDTO command = buildCommand("FILIAL01", "LEG001", "MV");

        when(jdbcTemplate.update(
                eq(SqlStatements.UPDATE_CENTRO_CUSTO),
                any(), any(), any(), any(), any(), any()
        )).thenReturn(1);

        int result = repository.update(command);

        assertEquals(1, result);

        verify(jdbcTemplate).update(
                SqlStatements.UPDATE_CENTRO_CUSTO,
                "Descricao Legado",
                "Descricao Protheus",
                "PRO001",
                "FILIAL01",
                "LEG001",
                "MV"
        );

    }

    private CommandDTO buildCommand(String filial, String codigoLegado, String sistemaLegado) {
        return new CommandDTO(
                1,
                filial,
                "Descricao Legado",
                codigoLegado,
                sistemaLegado,
                "Descricao Protheus",
                "PRO001"
        );
    }

}
