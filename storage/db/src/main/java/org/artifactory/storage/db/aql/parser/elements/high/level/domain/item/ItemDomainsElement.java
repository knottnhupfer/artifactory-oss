/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.storage.db.aql.parser.elements.high.level.domain.item;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.DomainProviderElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.EmptyIncludeDomainElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.IncludeDomainElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

import static org.artifactory.aql.model.AqlDomainEnum.*;
import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class ItemDomainsElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithDomainFields(list);
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithDomainFields(List<ParserElement> list) {
        list.add(new IncludeDomainElement(items));
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(archives.signature),
                fork(new EmptyIncludeDomainElement(archives), forward(dot, archiveDomains))));
        list.add(forward(new InternalNameElement(artifacts.signature),
                fork(new EmptyIncludeDomainElement(artifacts), forward(dot, buildArtifactDomains))));
        list.add(forward(new InternalNameElement(dependencies.signature),
                fork(new EmptyIncludeDomainElement(dependencies), forward(dot, buildDependenciesDomains))));
        list.add(forward(new InternalNameElement(statistics.signature),
                fork(new EmptyIncludeDomainElement(statistics), forward(dot, statisticsDomains))));
        list.add(forward(new InternalNameElement(properties.signature),
                fork(new EmptyIncludeDomainElement(properties), forward(dot, propertiesDomains))));
        list.add(forward(new InternalNameElement(releaseBundleFiles.signature),
                fork(new EmptyIncludeDomainElement(releaseBundleFiles), forward(dot, releaseBundleFileDomains))));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return items;
    }
}
