package dev.aisandbox.server.engine;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
                // work out default value
                String defaultValue;
                try {
                    Method getter = builder.getClass().getMethod(
                            (rawType == Boolean.class ? "is" : "get")
                                    + name);
                    Object value = getter.invoke(builder);
                    defaultValue = value == null ? "none" : getter.invoke(builder).toString();
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    log.error("Error getting default value for {}", name);
                    defaultValue = "unknown";
                }
                log.info("Found default value {}", defaultValue);
                parameters.add(
                        new SimulationParameter(name,
                                rawType,
                                defaultValue
                        ));
            }
        }
        log.debug("Returning parameters: {}", parameters);
        return parameters;
    }

    public static void setParameter(SimulationBuilder builder, SimulationParameter parameter, String value) {
        if (parameter.type() == Boolean.class) {
            setParameter(builder, "set" + parameter.parameterName(), Boolean.parseBoolean(value));
        } else if (parameter.type() == Integer.class) {
            setParameter(builder, "set" + parameter.parameterName(), Integer.parseInt(value));
        } else if (parameter.type() == Double.class) {
            setParameter(builder, "set" + parameter.parameterName(), Double.parseDouble(value));
        } else if (parameter.type().isEnum()) {
            setEnumParameter(builder, parameter, value);
        } else {
            log.warn("Dont know how to set parameter of type {}", parameter.type());
        }
    }

    private static void setEnumParameter(SimulationBuilder builder, SimulationParameter parameter, String value) {
        try {
            Method targetMethod = builder.getClass().getMethod("set"+parameter.parameterName(), parameter.type());
            targetMethod.invoke(builder, createEnumInstance(value, parameter.type()));
        } catch (Exception e) {
            log.error("Error invoking setter for {}", parameter.parameterName(),e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> T createEnumInstance(String name, Type type) {
        return Enum.valueOf((Class<T>) type, name.toUpperCase());
    }

    private static void setParameter(SimulationBuilder builder, String method, Object value) {
        try {
            Method targetMethod = builder.getClass().getMethod(method, value.getClass());
            targetMethod.invoke(builder, value);
        } catch (Exception e) {
            log.error("Error invoking setter for {}", method);
        }
    }

}
