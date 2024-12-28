package dev.aisandbox.server.options;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommandLineOptionsTests {

    @Test
    public void testRunHighLowCards() {
        RuntimeOptions options = RuntimeUtils.parseCommandLine("-s=HighLowCards --output=screen".split(" "));
        assertEquals(RuntimeOptions.RuntimeCommand.RUN,options.command(),"Run command not correct");
        assertEquals("HighLowCards",options.simulation(),"Simulation name not correct");
        assertEquals(RuntimeOptions.OutputOptions.SCREEN,options.output(),"Output not correct");
    }

    @Test
    public void testListSimulations() {
        RuntimeOptions options = RuntimeUtils.parseCommandLine("--list".split(" "));
        assertEquals(RuntimeOptions.RuntimeCommand.LIST,options.command(),"List command not correct");
        assertNull(options.simulation(),"Simulation name should be null");
    }

    @Test
    public void testListSimulationParameters() {
        RuntimeOptions options = RuntimeUtils.parseCommandLine("--list -s=HighLowCards".split(" "));
        assertEquals(RuntimeOptions.RuntimeCommand.LIST,options.command(),"List command not correct");
        assertEquals("HighLowCards",options.simulation(),"Simulation name not correct");
    }

    @Test
    public void testSetParameters() {
        RuntimeOptions options = RuntimeUtils.parseCommandLine("-s HighLowCards -p echo:true -c=3".split(" "));
        assertEquals(RuntimeOptions.RuntimeCommand.RUN,options.command(),"Run command not correct");
        assertEquals("HighLowCards",options.simulation(),"Simulation name not correct");
        assertEquals(3,options.agents(),"Number of agents not correct");
        assertNotNull(options.parameters(),"Parameters not correct");
        assertEquals(1,options.parameters().size(),"Number of parameters not correct");
        assertTrue(options.parameters().contains("echo:true"),"Echo parameter not correct");
    }

}
