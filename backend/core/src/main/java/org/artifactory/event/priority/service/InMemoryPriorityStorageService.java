package org.artifactory.event.priority.service;

import org.apache.commons.collections.MapUtils;
import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.event.priority.service.model.PrioritizedNodeEventMapper;
import org.artifactory.storage.db.event.entity.StoragePrioritizedNodeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.artifactory.event.priority.service.NodeEventPriorityServiceImpl.MAX_RETRIES;

/**
 * An in-memory replacement for Prioritized Node Events Storage.
 *
 * @author Uriah Levy
 */
public class InMemoryPriorityStorageService implements NodeEventPriorityStorageService {
    private static final Logger log = LoggerFactory.getLogger(InMemoryPriorityStorageService.class);
    private PrioritizedNodeEventMapper prioritizedEventMapper;
    private ConcurrentHashMap<String, Map<Long, StoragePrioritizedNodeEvent>> inMemoryPriorities = new ConcurrentHashMap<>();
    private final AtomicLong priorityIdIndex = new AtomicLong(0);

    InMemoryPriorityStorageService(PrioritizedNodeEventMapper prioritizedEventMapper) {
        this.prioritizedEventMapper = prioritizedEventMapper;
    }

    @Override
    public void savePriorityChart(NodeEventPriorityChart nodeEventPriorityChart) {
        nodeEventPriorityChart.getEvents().stream()
                .map(event -> prioritizedEventMapper.toStorageEvent(event))
                .forEach(event -> {
                    String operatorId = event.getOperatorId();
                    inMemoryPriorities.putIfAbsent(operatorId, new ConcurrentHashMap<>());
                    log.debug("Saving event priority with path '{}', and type '{}'", event.getPath(),
                            event.getType());
                    event.setPriorityId(priorityIdIndex.incrementAndGet());
                    inMemoryPriorities.get(operatorId).put(event.getPriorityId(), event);
                });
    }

    @Override
    public List<PrioritizedNodeEvent> getCurrentPriorityBatch(String operatorId) {
        OptionalInt minimalPriorityByOperatorId = getMinimalPriorityByOperatorId(operatorId);
        if (minimalPriorityByOperatorId.isPresent() && minimalPriorityByOperatorId.getAsInt() > 0) {
            log.debug("Current minimal priority is {}", minimalPriorityByOperatorId.getAsInt());
            return getEventsByPriorityAndOperatorId(minimalPriorityByOperatorId.getAsInt(), operatorId)
                    .stream()
                    .filter(prioritizedEvent -> prioritizedEvent.getRetryCount() < MAX_RETRIES)
                    .map(prioritizedEventMapper::toEvent)
                    .collect(Collectors.toList());
        } else {
            log.debug("No prioritized node events were found");
        }
        return Collections.emptyList();
    }

    @Override
    public boolean priorityExists(PrioritizedNodeEvent prioritizedNodeEvent) {
        return inMemoryPriorities.get(prioritizedNodeEvent.getOperatorId()).get(prioritizedNodeEvent.getPriorityId()) !=
                null;
    }

    @Override
    public void deleteEventPriority(PrioritizedNodeEvent prioritizedNodeEvent) {
        inMemoryPriorities.get(prioritizedNodeEvent.getOperatorId()).remove(prioritizedNodeEvent.getPriorityId());
    }

    @Override
    public boolean priorityListIsEmpty(String operatorId) {
        return MapUtils.isEmpty(inMemoryPriorities.get(operatorId));
    }

    @Override
    public void saveRetryCount(PrioritizedNodeEvent prioritizedNodeEvent) {
        StoragePrioritizedNodeEvent priority = inMemoryPriorities.get(prioritizedNodeEvent.getOperatorId())
                .get(prioritizedNodeEvent.getPriorityId());
        if (priority != null) {
            int retryCount = prioritizedNodeEvent.getRetryCount();
            log.debug("Incrementing retry count on event with path '{}' to {}", prioritizedNodeEvent.getPath(),
                    retryCount);
            priority.setRetryCount(prioritizedNodeEvent.getRetryCount());
            if (retryCount >= MAX_RETRIES) {
                log.warn("Retry attempts exhausted at '{}' retries for '{}'", retryCount,
                        prioritizedNodeEvent.getPath());
                deleteEventPriority(prioritizedNodeEvent);
            }
        }
    }

    @Override
    public boolean isDistributed() {
        return false;
    }

    private List<StoragePrioritizedNodeEvent> getEventsByPriorityAndOperatorId(int priority, String operatorId) {
        Map<Long, StoragePrioritizedNodeEvent> operatorPriorities = inMemoryPriorities.get(operatorId);
        if (MapUtils.isNotEmpty(operatorPriorities)) {
            return operatorPriorities
                    .values()
                    .stream()
                    .filter(p -> p.getOperatorId().equals(operatorId))
                    .filter(p -> p.getPriority() == priority)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private OptionalInt getMinimalPriorityByOperatorId(String operatorId) {
        Map<Long, StoragePrioritizedNodeEvent> operatorPriorities = inMemoryPriorities.get(operatorId);
        if (MapUtils.isNotEmpty(operatorPriorities)) {
            return operatorPriorities
                    .values()
                    .stream()
                    .filter(p -> p.getOperatorId().equals(operatorId))
                    .mapToInt(StoragePrioritizedNodeEvent::getPriority)
                    .filter(priority -> priority > 0)
                    .min();
        }
        return OptionalInt.empty();
    }
}
