package io.platformbuilder.depara.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilialJdbcRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private FilialJdbcRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FilialJdbcRepository(jdbcTemplate);
    }

    @Test
    void deveRetornarSetVazioQuandoCodigosFilialForNulo() {
        Set<String> result = repository.findExistingCodigosProtheus(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void deveRetornarSetVazioQuandoCodigosFilialForVazio() {
        Set<String> result = repository.findExistingCodigosProtheus(Collections.emptySet());
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void deveConsultarFiliaisExistentesERetornarSetSemDuplicidade() {

        Set<String> codigosFilial = new LinkedHashSet<>(Arrays.asList("02004015", "02004016"));

        when(jdbcTemplate.query(any(String.class), any(Object[].class), any(RowMapper.class)))
                .thenReturn(Arrays.asList("02004015", "02004015", "02004016"));

        Set<String> result = repository.findExistingCodigosProtheus(codigosFilial);

        assertEquals(2, result.size());
        assertTrue(result.contains("02004015"));
        assertTrue(result.contains("02004016"));

    }

    @Test
    void deveMontarSqlComQuantidadeCorretaDePlaceholdersEPassarParametrosAoJdbcTemplate() {

        Set<String> codigosFilial = new LinkedHashSet<>(Arrays.asList("02004015", "02004016", "02004017"));

        when(jdbcTemplate.query(any(String.class), any(Object[].class), any(RowMapper.class)))
                .thenReturn(Collections.singletonList("02004015"));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);

        repository.findExistingCodigosProtheus(codigosFilial);

        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(), any(RowMapper.class));

        String sql = sqlCaptor.getValue();
        Object[] params = paramsCaptor.getValue();

        assertAll(
                () -> assertTrue(sql.contains("SELECT CODIGO_PROTHEUS")),
                () -> assertTrue(sql.contains("FROM NEXTDIGITAL.FILIAL")),
                () -> assertTrue(sql.contains("WHERE CODIGO_PROTHEUS IN (?, ?, ?)")),
                () -> assertEquals(3, params.length),
                () -> assertTrue(Arrays.asList(params).contains("02004015")),
                () -> assertTrue(Arrays.asList(params).contains("02004016")),
                () -> assertTrue(Arrays.asList(params).contains("02004017"))
        );

    }

    @Test
    void deveRetornarSetVazioQuandoConsultaNaoEncontrarResultados() {

        Set<String> codigosFilial = new LinkedHashSet<>(Collections.singletonList("02004015"));

        when(jdbcTemplate.query(any(String.class), any(Object[].class), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        Set<String> result = repository.findExistingCodigosProtheus(codigosFilial);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
