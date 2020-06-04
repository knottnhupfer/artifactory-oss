package org.artifactory.api.rest.build;

import lombok.*;
import org.artifactory.api.rest.common.model.continues.ContinuePage;
import org.artifactory.build.BuildId;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ContinueBuildFilter extends ContinuePage {

    BuildId continueBuildId;
    OrderBy orderBy = OrderBy.BUILD_DATE;
    String searchStr;

    public ContinueBuildFilter(ContinuePage continuePage) {
        super(continuePage);
    }

    public ContinueBuildFilter(ContinueBuildFilter other) {
        super(other);
        this.orderBy = other.orderBy;
        this.searchStr = other.searchStr;
        this.continueBuildId = other.continueBuildId;
    }

    public String getOrderByStr() {
        return orderBy.name().toLowerCase();
    }

    public enum OrderBy {
        BUILD_NAME, BUILD_NUMBER, BUILD_ID, BUILD_DATE;
        public String lower() {
            return this.name().toLowerCase();
        }
    }

}
