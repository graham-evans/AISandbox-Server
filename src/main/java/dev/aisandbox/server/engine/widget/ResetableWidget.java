package dev.aisandbox.server.engine.widget;

/**
 * Denotes a widget that can be 'reset'. Reset-able widgets will delete any cached image and redraw themselves from new data the next time the draw method is called.
 */
public interface ResetableWidget {
    void reset();
}
