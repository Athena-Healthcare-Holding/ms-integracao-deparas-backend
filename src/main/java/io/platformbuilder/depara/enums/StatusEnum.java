package io.platformbuilder.depara.enums;

import lombok.Getter;

@Getter
public enum StatusEnum {

    INSERIDO, ATUALIZADO, ERRO;

    public static StatusEnum fromString(String status) {
        for (StatusEnum s : StatusEnum.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status desconhecido: " + status);
    }

}
