package com.github.lpedrosa.todo.actor.server;

import akka.actor.Props;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SaveSnapshotFailure;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.SnapshotMetadata;
import akka.persistence.SnapshotOffer;

import java.time.LocalDate;
import java.util.Collection;

import com.github.lpedrosa.todo.actor.server.message.reply.Entry;
import com.github.lpedrosa.todo.actor.server.message.request.AddEntry;
import com.github.lpedrosa.todo.actor.server.message.request.EntryAdded;
import com.github.lpedrosa.todo.actor.server.message.request.GetEntry;
import com.github.lpedrosa.todo.domain.TodoList;

public class TodoServer extends AbstractPersistentActor {

    private final String owner;
    private TodoList list;
    private int snapShotInterval = 2;

    public static Props props(String owner) {
        return Props.create(TodoServer.class, () -> new TodoServer(owner));
    }

    private TodoServer(String owner) {
        this.owner = owner;
        this.list = new TodoList();
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
            .match(EntryAdded.class, this::updateTodoList)
            .match(SnapshotOffer.class, this::updateToDoListFromSnapshot)
            .build();
    }

    private void updateTodoList(EntryAdded entryAddedEvt) {
        System.out.println("update todo list from EntryAdded");
        list.storeEntry(entryAddedEvt.getDate(), entryAddedEvt.getValue());
    }

    private void updateToDoListFromSnapshot(SnapshotOffer snapshotOffer) {
        //By default the last saved snapshot is passed to the actor before any events that follow the snapshot.
        System.out.println("update todo list from snapshot " + snapshotOffer.snapshot());
        list = (TodoList) snapshotOffer.snapshot();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(AddEntry.class, this::doAddEntry)
                .match(GetEntry.class, this::doGetEntry)
                .match(SaveSnapshotSuccess.class, this::getSnapShotSuccessMetaData)
                .match(SaveSnapshotFailure.class, this::getSnapShotFailureMetaData)
                .build();
    }

    public void doAddEntry(AddEntry msg) {
        LocalDate date = msg.getDate();
        String value = msg.getValue();

        EntryAdded entryAdded = new EntryAdded(date, value);
        persist(entryAdded, (EntryAdded) -> this.list.storeEntry(date, value));

        if (lastSequenceNr() % snapShotInterval == 0 && lastSequenceNr() != 0)
            saveSnapshot(list.copy());

    }

    public void doGetEntry(GetEntry msg) {
        LocalDate date = msg.getDate();

        Collection<String> tasksForDate = this.list.retrieveEntries(date);

        getSender().tell(new Entry(tasksForDate), getSelf());
    }

    private void getSnapShotSuccessMetaData(SaveSnapshotSuccess snapShot) {
        final SnapshotMetadata metadata = snapShot.metadata();
        System.out.println("snapshot save success " + metadata);
    }

    private void getSnapShotFailureMetaData(SaveSnapshotFailure snapshot) {
        final SnapshotMetadata metadata = snapshot.metadata();
        System.out.println("snapshot save failure " + metadata);
    }

    @Override
    public String persistenceId() {
        //unique per persistent actor
        return "to-do-server-persistence-id";
    }
}
