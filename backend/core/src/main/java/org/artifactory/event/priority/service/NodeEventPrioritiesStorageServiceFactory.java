package org.artifactory.event.priority.service;

import org.artifactory.common.ConstantValues;
import org.artifactory.event.priority.service.model.PrioritizedNodeEventMapper;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.storage.db.event.dao.NodeEventPrioritiesDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Uriah Levy
 */
@Configuration
public class NodeEventPrioritiesStorageServiceFactory {
    private static final Logger log = LoggerFactory.getLogger(NodeEventPrioritiesStorageServiceFactory.class);

    @Bean
    @Autowired
    @Scope("prototype")
    public NodeEventPriorityStorageService getStorageService(NodeEventPrioritiesDao prioritiesDao,
            PrioritizedNodeEventMapper mapper, InternalDbService dbService) {
        if (isPersisted()) {
            log.info("Initializing DB-based Priorities Storage Service");
            return new DistributedEventPriorityStorageService(prioritiesDao, mapper, dbService);
        } else {
            log.info("Initializing Memory Priorities Storage Service");
            return new InMemoryPriorityStorageService(mapper);
        }
    }

    public static boolean isPersisted() {
        String config = ConstantValues.metadataServerEventsPersistence.getString();
        return PersistenceType.fromName(config).equals(PersistenceType.DISTRIBUTED);
    }

    public enum PersistenceType {
        DISTRIBUTED,
        LOCAL;

        static PersistenceType fromName(String name) {
            for (PersistenceType type : values()) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            log.warn(
                    "Cannot associate user value '{}' with a Persistence Type. Falling back to default Persistence Type: distributed",
                    name);
            return DISTRIBUTED;
        }
    }
}
