package org.artifactory.api.rest.common.model.continues;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContinueIndexPage extends ContinuePage {

    Long continueIndex = 0L;

    public ContinueIndexPage(ContinuePage orig) {
        super(orig);
    }

    public ContinueIndexPage(ContinueIndexPage other) {
        super(other);
        this.continueIndex = other.continueIndex;
    }

}
