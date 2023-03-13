package com.exoreaction.xorcery.examples.forum;

import picocli.CommandLine;

public class Main
{
    public static void main(String[] args ) throws Exception
    {
        System.exit(new CommandLine(new com.exoreaction.xorcery.core.Main()).execute(args));
    }
}
