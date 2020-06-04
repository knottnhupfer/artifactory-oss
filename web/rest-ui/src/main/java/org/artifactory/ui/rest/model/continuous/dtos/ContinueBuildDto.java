package org.artifactory.ui.rest.model.continuous.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContinueBuildDto extends ContinuePageDto {

    String orderBy;
    String searchStr;

    public ContinueBuildDto(ArtifactoryRestRequest request) {
        super(request);
        Map<String, List<String>> params = request.getUriInfo().getQueryParameters();
        this.orderBy = getFirst(params, "orderBy");
        this.searchStr = getFirst(params, "searchStr");
    }

    public ContinueBuildDto(ContinueBuildDto other) {
        super(other);
        this.orderBy = other.orderBy;
        this.searchStr = other.searchStr;
    }

}
