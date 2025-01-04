package dev.aisandbox.server.options;

import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder
public record RuntimeOptions(
        RuntimeCommand command,
        String simulation,
        OutputOptions output,
        String outputDirectory,
        Integer agents,
        @Singular
        List<String> parameters
) {

    public enum RuntimeCommand {
        HELP, LIST, RUN
    }

    public enum OutputOptions {
        NONE, IMAGE, VIDEO, SCREEN
    }
}
