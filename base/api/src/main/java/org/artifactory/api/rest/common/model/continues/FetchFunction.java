package org.artifactory.api.rest.common.model.continues;

import lombok.Data;
import org.artifactory.api.rest.common.model.continues.ContinueResult;

/**
 *
 * @author Omri Ziv
 */
@Data
public class FetchFunction<T> {
    private IFetchFunc<ContinueResult<T>, Integer, Integer> function;
    private Long originalSize;

    public FetchFunction(IFetchFunc<ContinueResult<T>, Integer, Integer> function) {
        this.function = function;
        this.originalSize = 1L;
    }

    public FetchFunction(IFetchFunc<ContinueResult<T>, Integer, Integer> function, Long originalSize) {
        this.function = function;
        this.originalSize = originalSize;
    }

    @FunctionalInterface
    public static interface IFetchFunc<R,R2,R3> {
        R apply(R2 skip, R3 limit);
    }
}
