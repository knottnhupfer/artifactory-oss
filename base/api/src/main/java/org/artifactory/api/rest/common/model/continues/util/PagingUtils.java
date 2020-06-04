package org.artifactory.api.rest.common.model.continues.util;

import com.google.common.collect.Lists;
import org.artifactory.api.rest.common.model.continues.ContinueIndexPage;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.rest.common.model.continues.FetchFunction;

import java.util.List;


/**
 * Getting pagination results by Fetch Functions.
 * @author Omri Ziv
 */
public class PagingUtils {

    private PagingUtils() {

    }

    /**
     * Bring next page result. skipping function by it's collections original size.
     * @param continueState is the payload the method look at to determine the calculation terms.
     * @param fetchFunctions includes the calculations function and sizes.
     * @param <T> is the object type of the return element.
     * @return An object that includes data (as list) and continue state marker.
     */
    public static <T> ContinueResult<T> getPagingFromMultipleFunctions(ContinueIndexPage continueState,
            List<FetchFunction<T>> fetchFunctions) {
        return getPagingFromMultipleFunctions(
                continueState.getContinueIndex(),
                continueState.getLimit(),
                fetchFunctions);
    }

    static <T> ContinueResult<T> getPagingFromMultipleFunctions(long totalOffset, long pageSize,
            List<FetchFunction<T>> functions) {
        int functionIndex = 0;
        int scannedCount = 0;
        int howMuchGot = 0;
        long startAt = totalOffset;
        long resultContinueIndex = totalOffset;
        List<T> data = Lists.newArrayList();
        while (howMuchGot < pageSize && functionIndex < functions.size()) {
            FetchFunction<T> fetchFunction = functions.get(functionIndex++);
            long functionDataSize = fetchFunction.getOriginalSize();
            scannedCount += functionDataSize;
            if (startAt > scannedCount) {
                continue;
            }

            long offset = startAt == 0 ? 0 : functionDataSize - scannedCount + startAt;
            long limit = (int) (pageSize - howMuchGot);
            ContinueResult<T> continueResult = fetchFunction.getFunction().apply((int) offset, (int) limit);
            List<T> currentList = continueResult.getData();
            data.addAll(currentList);
            howMuchGot = data.size();
            startAt = 0;
            if (howMuchGot > 0) {
                resultContinueIndex = scannedCount - functionDataSize + Integer.parseInt(continueResult.getContinueState());
            }
        }
        String continueState = (data.size() < pageSize) ? null : resultContinueIndex + "";

        return new ContinueResult<>(continueState, data);
    }
}
