package io.platformbuilder.depara.mapper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FilialMapper {

    private FilialMapper() {
    }

    public static List<String> parse(String raw) {

        if (StringUtils.isBlank(raw)) return Collections.emptyList();

        String[] parts = raw.split(";");

        List<String> result = new ArrayList<>();

        Arrays.stream(parts)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .forEach(result::add);

        return result;
    }

}