package org.artifactory.event.priority.service;

import org.artifactory.event.priority.service.model.PrioritizedNodeEvent;
import org.artifactory.event.priority.service.model.PrioritizedNodeEventMapper;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.event.dao.NodeEventPrioritiesDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.artifactory.event.priority.service.NodeEventPriorityServiceImpl.MAX_RETRIES;

/**
 * @author Uriah Levy
 */
public class DistributedEventPriorityStorageService implements NodeEventPriorityStorageService {
    private static final Logger log = LoggerFactory.getLogger(DistributedEventPriorityStorageService.class);
    private PrioritizedNodeEventMapper prioritizedEventMapper;
    private InternalDbService dbService;
    private NodeEventPrioritiesDao eventPrioritiesDao;

    @Autowired
    public DistributedEventPriorityStorageService(NodeEventPrioritiesDao eventPrioritiesDao,
            PrioritizedNodeEventMapper prioritizedEventMapper, InternalDbService dbService) {
        this.eventPrioritiesDao = eventPrioritiesDao;
        this.prioritizedEventMapper = prioritizedEventMapper;
        this.dbService = dbService;
    }

    @Transactional
    public void savePriorityChart(NodeEventPriorityChart nodeEventPriorityChart) {
        nodeEventPriorityChart.getEvents().stream()
                .map(event -> prioritizedEventMapper.toStorageEvent(event))
                .forEach(event -> {
                    try {
                        log.debug("Saving event priority with path '{}', and type '{}'", event.getPath(),
                                event.getType());
                        event.setPriorityId(dbService.nextId());
                        eventPrioritiesDao.insertEventPriority(event);
                    } catch (SQLException e) {
                        throw new StorageException("Unable to save new event priority", e);
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    }
                });
    }

    @Override
    public List<PrioritizedNodeEvent> getCurrentPriorityBatch(String operatorID) {
        try {
            Optional<Integer> minimalPriorityByOperatorId = eventPrioritiesDao
                    .getMinimalPriorityByOperatorId(operatorID);
            if (minimalPriorityByOperatorId.isPresent() && minimalPriorityByOperatorId.get() > 0) {
                log.debug("Current minimal priority is {}", minimalPriorityByOperatorId.get());
                return eventPrioritiesDao
                        .getEventsByPriorityAndOperatorId(minimalPriorityByOperatorId.get(), operatorID)
                        .stream()
                        .filter(prioritizedEvent -> prioritizedEvent.getRetryCount() < MAX_RETRIES)
                        .map(prioritizedEventMapper::toEvent)
                        .collect(Collectors.toList());
            } else {
                log.debug("No prioritized node events were found");
            }
        } catch (SQLException e) {
            throw new StorageException("Unable to get current priority batch", e);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean priorityExists(PrioritizedNodeEvent prioritizedNodeEvent) {
        try {
            return eventPrioritiesDao.findPriorityById(prioritizedNodeEvent.getPriorityId()).isPresent();
        } catch (SQLException e) {
            throw new StorageException("Unable to check whether priority '" + prioritizedNodeEvent + "' exists", e);
        }
    }

    @Override
    public void deleteEventPriority(PrioritizedNodeEvent prioritizedNodeEvent) {
        try {
            eventPrioritiesDao.deleteEventPriorityById(prioritizedNodeEvent.getPriorityId());
        } catch (SQLException e) {
            throw new StorageException(
                    "Unable to delete event priority with ID " + prioritizedNodeEvent.getPriorityId(), e);
        }
    }

    @Override
    public boolean priorityListIsEmpty(String operatorId) {
        try {
            Optional<Integer> minimalPriorityByOperatorId = eventPrioritiesDao
                    .getMinimalPriorityByOperatorId(operatorId);
            if (minimalPriorityByOperatorId.isPresent()) {
                // The minimal ordinal priority is 1. Zero means the DB has no priorities for this operator
                return minimalPriorityByOperatorId.get() == 0;
            }
        } catch (SQLException e) {
            log.error("Unable to determine whether priority list is empty for operator with ID", e);
        }
        return false;
    }

    @Override
    public void saveRetryCount(PrioritizedNodeEvent prioritizedNodeEvent) {
        try {
            int retryCount = prioritizedNodeEvent.getRetryCount();
            log.debug("Incrementing retry count on event with path '{}' to {}", prioritizedNodeEvent.getPath(),
                    retryCount);
            eventPrioritiesDao.updateRetryCountById(prioritizedNodeEvent.getPriorityId(), retryCount);
            if (retryCount >= MAX_RETRIES) {
                log.warn("Retry attempts exhausted at '{}' retries for '{}'", retryCount,
                        prioritizedNodeEvent.getPath());
                deleteEventPriority(prioritizedNodeEvent);
            }
        } catch (SQLException e) {
            throw new StorageException(
                    "Unable to bump retry count on event with path " + prioritizedNodeEvent.getPath());
        }
    }

    @Override
    public boolean isDistributed() {
        return true;
    }
}
