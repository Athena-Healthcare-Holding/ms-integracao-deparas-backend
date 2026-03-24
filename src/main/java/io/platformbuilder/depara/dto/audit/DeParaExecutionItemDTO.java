package io.platformbuilder.depara.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class DeParaExecutionItemDTO {

    private final Integer linhaOrigemExcel;
    private final String nomeFilial;
    private final String codigoLegado;
    private final String codigoProtheus;
    private final String insertOracle;
    private final String updateOracle;
    private final String deleteOracle;

}
