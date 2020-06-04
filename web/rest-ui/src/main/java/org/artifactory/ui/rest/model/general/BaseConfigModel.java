package org.artifactory.ui.rest.model.general;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.rest.common.model.BaseModel;


/**
 * @author Omri Ziv
 */
@NoArgsConstructor
@Data
public class BaseConfigModel extends BaseModel {

    private String baseUrl;

    @Override
    public String toString() {
        return super.toString();
    }

}
