package dev.aisandbox.server.options;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public record ParameterEnumInfo(String parameterName, String parameterValue, List<String> enumValues) {

    @Override
    public String toString() {
        return parameterName+" {"+String.join(",",enumValues)+"}";
    }
}
