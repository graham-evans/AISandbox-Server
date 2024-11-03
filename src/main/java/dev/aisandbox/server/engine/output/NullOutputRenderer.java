package dev.aisandbox.server.engine.output;

public class NullOutputRenderer implements OutputRenderer{

    @Override
    public String getName() {
        return "none";
    }

    @Override
    public void display() {
        // do nothing;
    }

}
