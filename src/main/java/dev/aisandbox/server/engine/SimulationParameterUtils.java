package dev.aisandbox.server.engine;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@UtilityClass
public class SimulationParameterUtils {

    public static List<SimulationParameter> getParameters(SimulationBuilder builder) {
        log.debug("Querying parameters for {}", builder.getSimulationName());
        List<SimulationParameter> parameters = new ArrayList<>();
        for (Method method : builder.getClass().getMethods()) {
            if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                String name = method.getName().substring(3);
                Class<?> rawType = method.getParameterTypes()[0];
                log.debug("Found parameter named {}, with raw type {}", name, rawType);
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
                log.debug("Found default value {}", defaultValue);
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
            log.warn("Can't assign '{}' to this field", parameter.parameterName());
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

    public static Node createParameterEditor(SimulationParameter parameter) {
        BorderPane node = new BorderPane();
        // add label
        Label label = new Label(parameter.parameterName());
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_LEFT);
        node.setCenter(label);
        // add editor
        Node editor;
        if (parameter.type().isEnum()) {
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setItems(FXCollections.observableList(Arrays.stream(parameter.type().getEnumConstants()).map(Object::toString).toList()));
            comboBox.getSelectionModel().select(parameter.defaultValue());
            editor = comboBox;
        } else {
            editor = new Label("Unknown type");
        }
        node.setRight(editor);
        return node;
    }

}
