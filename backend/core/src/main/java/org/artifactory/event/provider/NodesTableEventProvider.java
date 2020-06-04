package org.artifactory.event.provider;

import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.storage.db.event.model.NodeEventCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 */
public class NodesTableEventProvider implements FiniteEventProvider<NodesTableEvent> {
    private static final Logger log = LoggerFactory.getLogger(NodesTableEventProvider.class);
    private AqlService aqlService;
    private Consumer<Integer> onBetweenBatches;
    private Runnable onFinish;
    private Function<NodeEventCursor, AqlApiItem> batchQuerySupplier;

    public NodesTableEventProvider(AqlService aqlService, Consumer<Integer> onBetweenBatches, Runnable onFinish,
            Function<NodeEventCursor, AqlApiItem> batchQuerySupplier) {
        this.aqlService = aqlService;
        this.onBetweenBatches = onBetweenBatches;
        this.onFinish = onFinish;
        this.batchQuerySupplier = batchQuerySupplier;
    }

    @Override
    public List<NodesTableEvent> provideNextBatch(NodeEventCursor currentCursor) {
        List<AqlItem> nextBatch = findNextBatch(currentCursor);
        if (nextBatch.isEmpty()) {
            log.debug("Nodes table event provider came up with an empty result set.");
            onFinish();
            return Collections.emptyList();
        }
        onBetweenBatches(nextBatch.size());
        return toNodesTableEvent(nextBatch);
    }

    private List<AqlItem> findNextBatch(NodeEventCursor currentCursor) {
        AqlEagerResult<AqlItem> queryResults = aqlService.executeQueryEager(batchQuerySupplier.apply(currentCursor));
        return queryResults.getResults();
    }

    private void onBetweenBatches(int size) {
        if (onBetweenBatches != null) {
            log.debug("Calling the on-between-batches callback");
            onBetweenBatches.accept(size);
        }
    }

    @Override
    public void onFinish() {
        if (onFinish != null) {
            log.debug("Triggering the finish callback");
            onFinish.run();
        }
    }

    private List<NodesTableEvent> toNodesTableEvent(List<AqlItem> results) {
        if (!results.isEmpty()) {
            return results.stream()
                    .map(aqlItem -> new NodesTableEvent(AqlUtils.fromAql(aqlItem).toPath(), aqlItem.getNodeId()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public ProviderType getType() {
        return ProviderType.NODES;
    }
}
