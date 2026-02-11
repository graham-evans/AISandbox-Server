/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.fx;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * A Logback appender that directs log output to a JavaFX TextArea component.
 *
 * <p>This custom appender bridges the gap between the standard Java logging framework (Logback)
 * and JavaFX UI components, allowing log messages to be displayed in real-time within the
 * application's user interface. It's particularly useful for showing live simulation logs,
 * debugging information, and status messages directly in the GUI.
 *
 * <p>The appender formats each log event's message and appends it to the configured TextArea,
 * followed by a system-appropriate line separator. This provides a live log view that updates as
 * the application runs.
 *
 * <p>Usage in logback.xml configuration:
 * <pre>{@code
 * <appender name="FX_TEXT_AREA" class="dev.aisandbox.server.fx.FXLogbackAppender">
 *     <!-- TextArea must be set programmatically -->
 * </appender>
 * }</pre>
 *
 * <p>Note: The TextArea reference must be set programmatically after creating the appender, as
 * JavaFX components cannot be instantiated through XML configuration.
 *
 * @see AppenderBase
 * @see ILoggingEvent
 * @see TextArea
 */
@Setter
@RequiredArgsConstructor
public class FXLogbackAppender extends AppenderBase<ILoggingEvent> {

  /**
   * The JavaFX TextArea where log messages will be displayed.
   */
  private final TextArea textArea;

  /**
   * Appends a formatted log message to the configured TextArea.
   *
   * <p>This method is called by the Logback framework whenever a log event needs to be processed.
   * It extracts the formatted message from the log event and adds it to the TextArea, followed by
   * a platform-specific line separator.
   *
   * <p>The method runs on the JavaFX Application Thread to ensure thread-safe UI updates.
   *
   * @param iLoggingEvent the log event containing the message and metadata to append
   */
  @Override
  protected void append(ILoggingEvent iLoggingEvent) {
    textArea.appendText(iLoggingEvent.getFormattedMessage());
    textArea.appendText(System.lineSeparator());
  }
}
