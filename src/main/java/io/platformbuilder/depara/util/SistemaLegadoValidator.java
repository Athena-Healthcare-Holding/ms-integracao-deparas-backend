package io.platformbuilder.depara.util;


import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class SistemaLegadoValidator {

    private static final Set<String> SISTEMAS_VALIDOS = new HashSet<>(
            Arrays.asList("SOLUS", "MV", "TASY")
    );

    private SistemaLegadoValidator() {
    }

    public static boolean isValido(String sistema) {

        if (StringUtils.isBlank(sistema)) {
            return false;
        }

        String normalizado = sistema.trim().toUpperCase();

        return SISTEMAS_VALIDOS.contains(normalizado);
    }

    public static String normalizar(String sistema) {

        if (StringUtils.isBlank(sistema)) {
            return null;
        }

        return sistema.trim().toUpperCase();
    }

}
