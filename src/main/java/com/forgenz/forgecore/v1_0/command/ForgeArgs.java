package com.forgenz.forgecore.v1_0.command;

import java.util.Iterator;

public final class ForgeArgs
        implements Iterable<String> {
    private final String command;
    private final String subCommandAlias;
    private final String[] args;

    public ForgeArgs(String command, String[] args) {
        this.command = command;
        this.subCommandAlias = args[0].trim().toLowerCase();
        this.args = new String[args.length - 1];

        for (int i = 1; i < args.length; i++) {
            this.args[(i - 1)] = args[i].trim();
        }
    }

    public String getCommandUsed() {
        return this.command;
    }

    public String getSubCommandAlias() {
        return this.subCommandAlias;
    }

    public int getNumArgs() {
        return this.args.length;
    }

    public String getArg(int i) {
        return this.args[i];
    }

    public Iterator<String> iterator() {
        return new ArgIterator();
    }

    public class ArgIterator implements Iterator<String> {
        private int index = 0;

        public ArgIterator() {
        }

        public boolean hasNext() {
            return this.index < ForgeArgs.this.getNumArgs();
        }

        public String next() {
            return ForgeArgs.this.getArg(this.index++);
        }

        public void remove() {
        }
    }
}