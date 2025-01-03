package dev.aisandbox.server.simulation;


import dev.aisandbox.launcher.SandboxServerCLIApplication;
import dev.aisandbox.server.engine.SimulationBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SandboxServerCLIApplication.class)
public class UniquenessTests {

    @Autowired
    List<SimulationBuilder> builderList;

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
