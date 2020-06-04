package org.artifactory.ui.rest.service.builds.search;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Lior Gur
 */
@Builder
@Data
public class BuildsSearchFilter {

    private String name;
    private List<String> numbers;
    private long before;
    private long after;
    private long limit;
    private long offset;
    private long daoLimit;
    private String orderBy;
    private String direction;
}
