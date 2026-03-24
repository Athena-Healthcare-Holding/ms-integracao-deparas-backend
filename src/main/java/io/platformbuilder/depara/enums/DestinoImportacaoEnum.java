package io.platformbuilder.depara.enums;

import lombok.Getter;

@Getter
public enum DestinoImportacaoEnum {

    CONTA_CONTABIL_OPERADORA("conta-contabil-operadora"),
    CENTRO_CUSTO("centro-custo");

    private final String pathValue;

    DestinoImportacaoEnum(String pathValue) {
        this.pathValue = pathValue;
    }

    public static DestinoImportacaoEnum fromPathValue(String value) {

        for (DestinoImportacaoEnum item : values()) {
            if (item.getPathValue().equalsIgnoreCase(value)) {
                return item;
            }
        }

        throw new IllegalArgumentException("Destino de importação inválido: " + value);
    }

}