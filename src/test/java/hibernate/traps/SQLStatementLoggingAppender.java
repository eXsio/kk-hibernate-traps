package hibernate.traps;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SQLStatementLoggingAppender<E> extends ConsoleAppender<E> {

    private static List<String> logs = new ArrayList<String>();

    @Override
    public void doAppend(E eventObject) {
        LoggingEvent event = (LoggingEvent) eventObject;
        logs.add(event.getMessage());
        super.doAppend(eventObject);
    }

    public static void clear() {
        logs.clear();
    }

    public static void printAll() {
        System.out.println(String.format("There are %d SQL Statement(s) collected by the Logger:", logs.size()));
        logs.forEach(System.out::println);
    }

    public static void printQueriesContaining(String queryPart) {
        List<String> filtered = logs.stream().filter(l -> l.contains(queryPart)).collect(Collectors.toList());
        System.out.println(String.format("There are %d SQL Statement(s) collected by the Logger, that contain '%s':", filtered.size(), queryPart));
        filtered.forEach(System.out::println);
    }

    public static int countQueriesContaining(String queryPart) {
        return (int) logs.stream().filter(l -> l.contains(queryPart)).count();
    }

    public static int size() {
        return logs.size();
    }
}
