package dev.aisandbox.server.fx;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@RequiredArgsConstructor
public class FXLogbackAppender extends AppenderBase<ILoggingEvent> {

    private final TextArea textArea;

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        textArea.appendText(iLoggingEvent.getFormattedMessage());
        textArea.appendText(System.lineSeparator());
    }
}
