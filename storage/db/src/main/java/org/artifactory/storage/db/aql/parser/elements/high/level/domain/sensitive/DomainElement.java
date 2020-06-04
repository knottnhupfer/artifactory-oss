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

package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.DomainSubPathElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.action.DeleteElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.action.FindElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.action.UpdateElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.include.IncludeExtensionElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.language.SectionEndElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class DomainElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        ParserElement tail = fork(
                // .find() supported for all domains
                forward(
                        fork(empty, forward(dot, provide(FindElement.class), new SectionEndElement())),
                        fork(empty, forward(dot, provide(IncludeExtensionElement.class), new SectionEndElement())),
                        fork(empty, forward(dot, provide(SortExtensionElement.class), new SectionEndElement())),
                        fork(empty, forward(dot, offset), new SectionEndElement()),
                        fork(empty, forward(dot, limit), new SectionEndElement())
                ),
                // Add additional possible paths based on domain
                getChainByDomain());
        return createDomainParserElement(domain, tail);
    }

    // Still ugly...
    private ParserElement getChainByDomain() {
        ParserElement tail = empty;
        switch (domain) {
            case items: {
                tail = forward(
                        fork(empty, forward(dot, provide(DeleteElement.class), new SectionEndElement())),
                        fork(empty, forward(dot, provide(IncludeExtensionElement.class), new SectionEndElement())),
                        fork(empty, forward(dot, dryRun), new SectionEndElement()));
                break;
            }
            case properties:
                tail = forward(
                        fork(empty, forward(dot, provide(UpdateElement.class), new SectionEndElement(), dot, actionPropertyKeys, new SectionEndElement(), dot, actionValue, new SectionEndElement())),
                        fork(empty, forward(dot, provide(IncludeExtensionElement.class), new SectionEndElement())),
                        fork(empty, forward(dot, dryRun), new SectionEndElement()));
                break;
        }
        return tail;
    }

    private ParserElement createDomainParserElement(AqlDomainEnum value, ParserElement tail) {
        String[] domainSubPaths = value.subDomains;
        ParserElement[] domainNameElement = new ParserElement[domainSubPaths.length * 2];
        for (int j = 0; j < domainSubPaths.length; j++) {
            ParserElement parserElement = new DomainSubPathElement(domainSubPaths[j]);
            domainNameElement[j * 2] = parserElement;
            domainNameElement[j * 2 + 1] = dot;
        }
        domainNameElement[domainNameElement.length - 1] = tail;
        return forward(domainNameElement);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
