package dev.aisandbox.server.options;

import dev.aisandbox.server.engine.SimulationBuilder;
import dev.aisandbox.server.engine.SimulationParameter;
import dev.aisandbox.server.engine.SimulationParameterUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimulationOptionReader {

    private final List<SimulationBuilder> builders;

    public SimulationBuilder readOptions(RuntimeOptions options) {
        // find correct builder
        SimulationBuilder builder = null;
        for (SimulationBuilder simulationBuilder : builders) {
            if (simulationBuilder.getSimulationName().equalsIgnoreCase(options.simulation())) {
                builder = simulationBuilder;
            }
        }
        if (builder == null) {
            log.error("Builder not found");
            return null;
        }
        // load the parameters and options
        List<SimulationParameter> parameters = SimulationParameterUtils.getParameters(builder);
        // set inputted parameters
        for (String parameter : options.parameters()) {
            String[] keyvalues = parameter.split("[=:]");
            if (keyvalues.length != 2) {
                log.warn("Couldn't parse \"{}\", ignoring", parameter);
            } else {
                String key = keyvalues[0];
                String value = keyvalues[1];
                // find a matching parameter
                Optional<SimulationParameter> oParam = parameters.stream().filter(simulationParameter -> simulationParameter.parameterName().equalsIgnoreCase(key)).findFirst();
                if (oParam.isPresent()) {
                    SimulationParameterUtils.setParameter(builder, oParam.get(), value);
                } else {
                    log.info("Can't find a parameter with name '{}' - ignoring", key);
                }
            }
        }
        return builder;
    }

}
