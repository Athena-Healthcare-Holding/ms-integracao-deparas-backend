package io.platformbuilder.depara.repository;

import io.platformbuilder.depara.dto.CommandDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;

import java.util.List;
import java.util.Set;

public interface ImportJdbcRepository {

    DestinoImportacaoEnum getDestino();

    Set<String> findExistingKeys(List<CommandDTO> commands);

    int[] batchInsert(List<CommandDTO> commands);

    int[] batchUpdate(List<CommandDTO> commands);

    int insert(CommandDTO command);

    int update(CommandDTO command);

}
