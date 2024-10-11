package dev.aisandbox.server.options;

import lombok.Builder;

@Builder
public record RuntimeOptions(RuntimeCommand command,String simulation,OutputOptions output,String outputDirectory) {

    public enum RuntimeCommand{
        HELP,LIST,RUN
    }

    public enum OutputOptions{
        NONE,PNG,SCREEN
    }
}
