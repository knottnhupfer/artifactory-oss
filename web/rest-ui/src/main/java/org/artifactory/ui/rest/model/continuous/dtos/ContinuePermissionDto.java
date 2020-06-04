package org.artifactory.ui.rest.model.continuous.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContinuePermissionDto extends ContinuePageDto {

    String searchStr;

    public ContinuePermissionDto(ArtifactoryRestRequest request) {
        super(request);
        Map<String, List<String>> params = request.getUriInfo().getQueryParameters();
        this.searchStr = getFirst(params, "searchStr");
    }

    public ContinuePermissionDto(ContinuePermissionDto other) {
        super(other);
        this.searchStr = other.searchStr;
    }

}
