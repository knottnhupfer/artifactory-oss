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

package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.dynamic;

import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.DomainSensitiveParserElement;

/**
 * @author Gidi Shabat
 */
public class DynamicDomain extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        switch (domain) {
            case items: 
                return AqlParser.itemDomains;
            case archives: 
                return AqlParser.archiveDomains;
            case entries: 
                return AqlParser.entriesDomains;
            case properties: 
                return AqlParser.propertiesDomains;
            case statistics: 
                return AqlParser.statisticsDomains;
            case artifacts: 
                return AqlParser.buildArtifactDomains;
            case dependencies: 
                return AqlParser.buildDependenciesDomains;
            
            case builds: 
                return AqlParser.buildDomains;
            
            case modules: 
                return AqlParser.buildModuleDomains;
            
            case moduleProperties: 
                return AqlParser.buildModulePropertiesDomains;
            
            case buildPromotions: 
                return AqlParser.buildPromotionsDomains;
            
            case buildProperties: 
                return AqlParser.buildPropertiesDomains;
            
            case releaseBundles: 
                return AqlParser.releaseBundleDomains;
            
            case releaseBundleFiles: 
                return AqlParser.releaseBundleFileDomains;
            
        }
        throw new UnsupportedOperationException("Unsupported domain :" + domain);
    }
}