package com.github.lpedrosa.todo.actor.server.message.reply;

import java.io.Serializable;
import java.util.Collection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public class Entry implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ImmutableList<String> tasks;

    public Entry(Collection<String> tasks) {
        this.tasks = ImmutableList.copyOf(tasks);
    }

    public ImmutableCollection<String> getTasks() {
        return this.tasks;
    }

}
