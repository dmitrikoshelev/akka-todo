package com.github.lpedrosa.todo.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

public class TodoList implements Serializable {

    private static final long serialVersionUID = 1L;

    private  ListMultimap<LocalDate, String> entries;

    public TodoList() {
        this.entries = MultimapBuilder.ListMultimapBuilder
                .hashKeys()
                .arrayListValues()
                .build();
    }

    public TodoList(ListMultimap<LocalDate, String> entries) {
        this.entries = entries;
    }

    public void storeEntry(LocalDate date, String entry) {
        this.entries.put(date, entry);
    }

    public TodoList copy() {
        return new TodoList(entries);
    }

    public Collection<String> retrieveEntries(LocalDate date) {
        // return copy since underlying reference allows changes to the entries field
        return new ArrayList<>(this.entries.get(date));
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("entries", entries)
            .toString();
    }
}
