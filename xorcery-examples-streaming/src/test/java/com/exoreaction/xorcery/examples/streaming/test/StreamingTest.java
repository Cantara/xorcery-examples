package com.exoreaction.xorcery.examples.streaming.test;

import dev.xorcery.runner.Main;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class StreamingTest {

    @Test
    public void testStreaming()
    {
        new CommandLine(new Main()).execute();
    }
}
