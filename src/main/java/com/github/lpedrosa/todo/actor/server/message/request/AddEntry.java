package com.github.lpedrosa.todo.actor.server.message.request;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public class AddEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate date;
    private final String value;

    public AddEntry(LocalDate date, String value) {
        this.date = Objects.requireNonNull(date);
        this.value = Objects.requireNonNull(value);
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("date", this.date.format(DateTimeFormatter.BASIC_ISO_DATE))
                .add("value", this.value)
                .toString();
    }

}
