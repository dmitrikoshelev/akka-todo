package com.github.lpedrosa.todo.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.iq80.leveldb.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.lpedrosa.todo.actor.server.message.reply.Entry;
import com.github.lpedrosa.todo.actor.server.message.request.AddEntry;
import com.github.lpedrosa.todo.actor.server.message.request.GetEntry;
import com.google.common.collect.ImmutableCollection;

public class TodosTests {

    private static ActorSystem system;
    private static Todos todos;

    @Before
    public void setup() {
        system = ActorSystem.create();
        todos = new Todos(system);
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
        FileUtils.deleteDirectoryContents(new File("/home/dmitri/nexmo/workspace/akka-todo/target"));
    }

    @Test
    public void shouldAlwaysGetSameActorForName() throws Throwable {
        ActorRef bob = todos.listFor("bob");
        ActorRef sameBob = todos.listFor("bob");

        assertEquals(bob, sameBob);
    }

    @Test
    public void serverShouldWorkProperly() throws Throwable {
        ActorRef bob = todos.listFor("bob");

        final LocalDate time = LocalDate.now();
        final String task = "Do the dishes";

        AddEntry add = new AddEntry(time, task);
        bob.tell(add, ActorRef.noSender());

        TestKit probe = new TestKit(system);

        GetEntry get = new GetEntry(time);
        bob.tell(get, probe.getRef());

        Entry reply = probe.expectMsgClass(Duration.apply(1, TimeUnit.SECONDS),
            Entry.class);

        ImmutableCollection<String> tasks = reply.getTasks();
        assertEquals(1, tasks.size());

        for (String t : tasks) {
            assertEquals(task, t);
        }
    }

    @Test
    public void testEventsRetrievedInOrderTheyWerePersisted() throws Throwable {
        ActorRef bob = todos.listFor("bob");
        final String [] tasks =  {"Do the dishes", "Wash the floor"};

        final LocalDate time = LocalDate.now();

        ImmutableCollection<String> persistedTasks = sendCommand(time, bob, tasks[0]);
        assertEquals(1, persistedTasks.size());

        testOrderOfPersistedEvents(persistedTasks, tasks);

        system.stop(bob);
        Thread.sleep(1000);
        assertTrue(bob.isTerminated());

        bob = todos.listFor("bob");

        persistedTasks = sendCommand(time, bob, tasks[1]);
        assertEquals(2, persistedTasks.size());

        testOrderOfPersistedEvents(persistedTasks, tasks);
    }

    private ImmutableCollection<String> sendCommand(LocalDate time, ActorRef bob, String task) {
        AddEntry add1 = new AddEntry(time, task);
        bob.tell(add1, ActorRef.noSender());

        TestKit probe = new TestKit(system);

        GetEntry get = new GetEntry(time);
        bob.tell(get, probe.getRef());

        Entry reply = probe.expectMsgClass(Duration.apply(1, TimeUnit.SECONDS), Entry.class);

        return reply.getTasks();
    }

    private void testOrderOfPersistedEvents(ImmutableCollection<String> persistedTasks, String [] tasks) {
        int i = 0;
        for (String t : persistedTasks) {
            assertEquals(tasks[i], t);
            i++;
        }
    }

}
