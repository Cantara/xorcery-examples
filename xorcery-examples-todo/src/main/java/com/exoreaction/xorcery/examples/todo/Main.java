package com.exoreaction.xorcery.examples.todo;

import picocli.CommandLine;

public class Main
{
    public static void main(String[] args ) throws Exception
    {
        System.exit(new CommandLine(new com.exoreaction.xorcery.runner.Main()).execute(args));
    }
}
