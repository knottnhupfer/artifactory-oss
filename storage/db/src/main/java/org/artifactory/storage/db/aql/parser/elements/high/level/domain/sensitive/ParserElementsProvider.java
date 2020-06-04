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

import org.artifactory.aql.AqlParserException;
import org.artifactory.aql.model.AqlDomainEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gidi Shabat
 */
public class ParserElementsProvider {

    private Map<AqlDomainEnum, Map<Class, DomainSensitiveParserElement>> domainMap = new HashMap<>();

    public <T extends DomainSensitiveParserElement> T provide(Class<T> elementClass, AqlDomainEnum domain) {
        try {
            Map<Class, DomainSensitiveParserElement> map = domainMap.computeIfAbsent(domain, domainKey -> new HashMap<>());
            T parserElement = (T) map.get(elementClass);
            if (parserElement == null) {
                parserElement = elementClass.newInstance();
                parserElement.setDomain(domain);
                parserElement.setProvider(this);
                map.put(elementClass, parserElement);
            }
            return parserElement;
        } catch (Exception e) {
            throw new AqlParserException("Fail to init the parser", e);
        }
    }
}
