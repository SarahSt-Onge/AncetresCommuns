package pedigree;

/**
 * Chaque événement a un temps, un sujet, et un type.
 */

public class Event implements Comparable<Event> {
    public final double time;
    public final Sim subject;
    public final EventType type;

    public Event(double time, Sim subject, EventType type) {
        this.time = time;
        this.subject = subject;
        this.type = type;
    }

    @Override
    public int compareTo(Event other) {
        return Double.compare(this.time, other.time);
    }
}
