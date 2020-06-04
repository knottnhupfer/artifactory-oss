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

package org.artifactory.storage.db.aql.parser.elements.high.level.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.DomainElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.ParserElementsProvider;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import static org.artifactory.aql.model.AqlDomainEnum.*;


/**
 * Represent the AQL Language structure
 *
 * @author Gidi Shabat
 */
public class RootElement extends LazyParserElement {

    @Override
    protected ParserElement init() {
        ParserElementsProvider provider = new ParserElementsProvider();
        return fork(
                provider.provide(DomainElement.class, items),
                provider.provide(DomainElement.class, archives),
                provider.provide(DomainElement.class, entries),
                provider.provide(DomainElement.class, properties),
                provider.provide(DomainElement.class, statistics),
                provider.provide(DomainElement.class, artifacts),
                provider.provide(DomainElement.class, dependencies),
                provider.provide(DomainElement.class, modules),
                provider.provide(DomainElement.class, moduleProperties),
                provider.provide(DomainElement.class, buildProperties),
                provider.provide(DomainElement.class, buildPromotions),
                provider.provide(DomainElement.class, builds),
                provider.provide(DomainElement.class, releaseBundles),
                provider.provide(DomainElement.class, releaseBundleFiles));
    }
}
