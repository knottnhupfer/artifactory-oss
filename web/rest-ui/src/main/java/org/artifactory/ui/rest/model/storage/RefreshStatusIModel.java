package org.artifactory.ui.rest.model.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Inbar Tal
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshStatusIModel {
    private boolean isCalculating;
}
