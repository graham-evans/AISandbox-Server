package dev.aisandbox.server.simulation;


import dev.aisandbox.server.engine.SimulationBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UniquenessTests {

    private static final List<SimulationBuilder> builderList = new ArrayList<>();

    @BeforeAll
    static void setUpBeforeClass() {
        Arrays.stream(SimulationEnumeration.values()).forEach(simulationEnum -> builderList.add(simulationEnum.getBuilder()));
    }

    /**
     * Tests that there are the expected number of simulation builders available
     **/
    @Test
    public void includedTest() {
        assertNotNull(builderList);
        assertEquals(3, builderList.size());
    }

    /**
     * Tests that all simulation builders have unique names
     */
    @Test
    public void uniqueNameTest() {
        Set<String> names = new HashSet<>();
        builderList.forEach(builder -> {
            names.add(builder.getSimulationName().toLowerCase());
        });
        assertEquals(builderList.size(), names.size());
    }

    /**
     * Tests that no builders names have whitespace in them
     */
    @Test
    public void whitespaceTest() {
        for (SimulationBuilder builder : builderList) {
            assertFalse(builder.getSimulationName().contains(" "), builder.getSimulationName() + " contains whitespace");
        }
    }
}
