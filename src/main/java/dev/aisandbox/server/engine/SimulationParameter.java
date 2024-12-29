package dev.aisandbox.server.engine;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Optional;

public record SimulationParameter(String parameterName, ParameterType type, String defaultValue,
                                  Optional<String> options) {

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(parameterName);
        s.append("(");
        s.append(type);
        s.append(") - default=");
        s.append(defaultValue);
        if (options.isPresent()) {
            s.append(", options=");
            s.append(options.get());
        }
        return s.toString();
    }

    public enum ParameterType {
        INTEGER, BOOLEAN, ENUM, STRING,DOUBLE
    }

}
