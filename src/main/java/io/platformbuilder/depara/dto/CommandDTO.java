package io.platformbuilder.depara.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
public class CommandDTO {

    private Integer linhaOrigemExcel;
    private String nomeFilial;
    private String descricaoLegado;
    private String codigoLegado;
    private String sistemaLegado;
    private String descricaoProtheus;
    private String codigoProtheus;

    public String uniqueKey() {
        return nomeFilial + "|" + codigoLegado;
    }

    public String getSistemaLegado() {
        return Objects.nonNull(sistemaLegado) ? sistemaLegado.trim().toUpperCase() : null;
    }

    public void setSistemaLegado(String sistemaLegado) {
        this.sistemaLegado = Objects.nonNull(sistemaLegado) ? sistemaLegado.trim().toUpperCase() : null;
    }

}
