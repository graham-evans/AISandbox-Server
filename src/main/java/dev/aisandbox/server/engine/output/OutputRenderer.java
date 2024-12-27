package dev.aisandbox.server.engine.output;

public interface OutputRenderer {
    public String getName();
    public void display();
    public void close();
}
