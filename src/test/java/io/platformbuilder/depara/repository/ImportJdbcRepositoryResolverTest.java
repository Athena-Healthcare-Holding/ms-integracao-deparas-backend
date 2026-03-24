package io.platformbuilder.depara.repository;

import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportJdbcRepositoryResolverTest {

    @Test
    void deveRetornarRepositoryCorretoParaDestino() {

        ImportJdbcRepository repoCentroCusto = mock(ImportJdbcRepository.class);
        ImportJdbcRepository repoConta = mock(ImportJdbcRepository.class);

        when(repoCentroCusto.getDestino()).thenReturn(DestinoImportacaoEnum.CENTRO_CUSTO);
        when(repoConta.getDestino()).thenReturn(DestinoImportacaoEnum.CONTA_CONTABIL_OPERADORA);

        ImportJdbcRepositoryResolver resolver = new ImportJdbcRepositoryResolver(
                Arrays.asList(repoCentroCusto, repoConta)
        );

        ImportJdbcRepository result = resolver.resolve(DestinoImportacaoEnum.CONTA_CONTABIL_OPERADORA);

        assertEquals(repoConta, result);

    }

    @Test
    void deveLancarExcecaoQuandoNaoEncontrarRepository() {

        ImportJdbcRepository repoCentroCusto = mock(ImportJdbcRepository.class);
        when(repoCentroCusto.getDestino()).thenReturn(DestinoImportacaoEnum.CENTRO_CUSTO);

        ImportJdbcRepositoryResolver resolver = new ImportJdbcRepositoryResolver(
                Collections.singletonList(repoCentroCusto)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(DestinoImportacaoEnum.CONTA_CONTABIL_OPERADORA)
        );

        assertTrue(exception.getMessage().contains("Nenhum repository encontrado"));

    }

    @Test
    void deveRetornarPrimeiroRepositoryQueCorresponder() {

        ImportJdbcRepository repo1 = mock(ImportJdbcRepository.class);
        ImportJdbcRepository repo2 = mock(ImportJdbcRepository.class);

        when(repo1.getDestino()).thenReturn(DestinoImportacaoEnum.CENTRO_CUSTO);
        when(repo2.getDestino()).thenReturn(DestinoImportacaoEnum.CENTRO_CUSTO);

        ImportJdbcRepositoryResolver resolver = new ImportJdbcRepositoryResolver(
                Arrays.asList(repo1, repo2)
        );

        ImportJdbcRepository result = resolver.resolve(DestinoImportacaoEnum.CENTRO_CUSTO);

        assertEquals(repo1, result);

    }

}
