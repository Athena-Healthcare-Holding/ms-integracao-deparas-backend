package io.platformbuilder.depara.repository;


import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolver para obter a implementação correta de ImportJdbcRepository com base no DestinoImportacaoEnum.
 */
@Component
public class ImportJdbcRepositoryResolver {

    private final List<ImportJdbcRepository> repositories;

    public ImportJdbcRepositoryResolver(List<ImportJdbcRepository> repositories) {
        this.repositories = repositories;
    }

    public ImportJdbcRepository resolve(DestinoImportacaoEnum destino) {

        for (ImportJdbcRepository repository : repositories) {
            if (repository.getDestino() == destino) {
                return repository;
            }
        }

        throw new IllegalArgumentException("Nenhum repository encontrado para o destino: " + destino);
    }

}
