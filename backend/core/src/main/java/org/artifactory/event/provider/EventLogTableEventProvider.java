package org.artifactory.event.provider;

import org.artifactory.common.ConstantValues;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.artifactory.storage.event.EventInfo;
import org.artifactory.storage.event.EventsService;
import org.jfrog.common.ClockUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Uriah Levy
 * An event provider that reads from the event_log table
 */
public class EventLogTableEventProvider implements EventProvider<EventInfo> {

    private static final int BATCH_SIZE = 5000;
    private EventsService eventsService;

    public EventLogTableEventProvider(EventsService eventsService) {
        this.eventsService = eventsService;
    }

    @Override
    public List<EventInfo> provideNextBatch(NodeEventCursor currentCursor) {
        long currentCursorTimestamp = currentCursor.getEventMarker();
        // The unstable margin is a short time frame during which a TX has already recorded an event with a particular
        // timestamp, but hasn't committed it yet in the event log. We make sure to avoid those TX's by never performing
        // events that are as new as the current system time.
        long nowMinusUnstableMargin = getNowMinusUnstableMargin();
        try (Stream<EventInfo> eventsStream = eventsService.getEventsSince(currentCursorTimestamp)) {
            return eventsStream
                    .filter(event -> event.getTimestamp() < nowMinusUnstableMargin)
                    .limit(BATCH_SIZE)
                    .collect(Collectors.toList());
        }
    }

    private long getNowMinusUnstableMargin() {
        return ClockUtils.epochMillis() -
                TimeUnit.SECONDS.toMillis(ConstantValues.metadataEventUnstableMarginSecs.getLong());
    }

    @Override
    public ProviderType getType() {
        return ProviderType.EVENT_LOG;
    }
}
