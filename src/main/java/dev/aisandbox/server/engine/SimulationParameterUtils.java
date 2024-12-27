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
                parameters.add(
                        new SimulationParameter(name,
                                getParameterType(rawType),
                                getDefaultValue(builder, name),
                                getParameterOptions(rawType)
                        ));
            }
        }
        log.debug("Returning parameters: {}", parameters);
        return parameters;
    }

    private SimulationParameter.ParameterType getParameterType(Class<?> clazz) {
        log.debug("Converting parameter type from {}", clazz.getName());
        if (clazz.isEnum()) {
            return SimulationParameter.ParameterType.ENUM;
        }
        // TODO, detect other types
        return switch (clazz.getName()) {
            default -> SimulationParameter.ParameterType.STRING;
        };
    }

    private Optional<String> getParameterOptions(Class<?> clazz) {
        if (clazz.isEnum()) {
            return Optional.of(Arrays.stream(clazz.getEnumConstants()).map(o -> o.toString()).collect(Collectors.joining(",")));
        } else {
            return Optional.empty();
        }
    }

    public static String getDefaultValue(SimulationBuilder builder, String parameterName) {
        try {
            Method getter = builder.getClass().getMethod("get" + parameterName);
            return getter.invoke(builder).toString();
        } catch (NoSuchMethodException e) {
            log.error("Error getting default value for {}", parameterName);
        } catch (InvocationTargetException e) {
            log.error("Invocation target error getting default value for {}", parameterName);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccess error getting default value for {}", parameterName);
        }
        return "unknown";
    }

    public static void setParameter(SimulationBuilder builder, String name, int value) {

    }


}
