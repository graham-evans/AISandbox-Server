package dev.aisandbox.server.engine;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class SimulationParameterUtils {

    public static List<SimulationParameter> getParameters(SimulationBuilder builder) {
        log.debug("Querying parameters for {}", builder.getName());
        List<SimulationParameter> parameters = new ArrayList<>();
        for (Method method : builder.getClass().getMethods()) {
            if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                String name = method.getName().substring(3);
                Class<?> rawType = method.getParameterTypes()[0];
                log.info("Found parameter named {}, with raw type {}", name, rawType);
                // work out parameter type
                SimulationParameter.ParameterType parameterType;
                if (rawType.isEnum()) {
                    parameterType = SimulationParameter.ParameterType.ENUM;
                } else {
                    parameterType = switch (rawType.getName()) {
                        case "double" -> SimulationParameter.ParameterType.DOUBLE;
                        case "int" -> SimulationParameter.ParameterType.INTEGER;
                        case "boolean" -> SimulationParameter.ParameterType.BOOLEAN;
                        default -> SimulationParameter.ParameterType.STRING;
                    };
                }
                log.info("Converted parameter type to {}", parameterType);
                // work out default value
                String defaultValue;
                try {
                    Method getter = builder.getClass().getMethod(
                            (parameterType == SimulationParameter.ParameterType.BOOLEAN ? "is" : "get")
                                    + name);
                    Object value = getter.invoke(builder);
                    defaultValue = value == null ? "none" : getter.invoke(builder).toString();
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    log.error("Error getting default value for {}", name);
                    defaultValue = "unknown";
                }
                log.info("Found default value {}", defaultValue);
                // get parameter options
                Optional<String> options = Optional.empty();
                if (rawType.isEnum()) {
                    options = Optional.of(Arrays.stream(rawType.getEnumConstants()).map(o -> o.toString()).collect(Collectors.joining(",")));
                }
                parameters.add(
                        new SimulationParameter(name,
                                parameterType,
                                defaultValue,
                                options
                        ));
            }
        }
        log.debug("Returning parameters: {}", parameters);
        return parameters;
    }

    public static void setParameter(SimulationBuilder builder, SimulationParameter parameter, String value) {
        switch (parameter.type()) {
            case BOOLEAN -> setParameter(builder,"set"+parameter.parameterName(), Boolean.parseBoolean(value));
            case ENUM -> setEnumParameter(builder, "set"+parameter.parameterName(), value);
            default -> log.info("Dont know how to set parameter of type {}",parameter.type());
        }

    }

    private static void setEnumParameter(SimulationBuilder builder, String method, String value) {
        try {
            Method targetMethod = builder.getClass().getMethod(method, boolean.class);
            targetMethod.invoke(builder, value);
        } catch (Exception e) {
            log.error("Error invoking setter for {}", method);
        }
    }

    private static void setParameter(SimulationBuilder builder, String method,boolean value) {
        try {
            Method targetMethod = builder.getClass().getMethod(method, boolean.class);
            targetMethod.invoke(builder, value);
        } catch (Exception e) {
            log.error("Error invoking setter for {}", method);
        }
    }

}
