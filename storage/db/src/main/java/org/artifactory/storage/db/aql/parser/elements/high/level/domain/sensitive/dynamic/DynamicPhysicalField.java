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
public class DynamicPhysicalField extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        switch (domain) {
            case items: {
                return AqlParser.itemPhysicalFields;
            }
            case archives: {
                return AqlParser.archivePhysicalFields;
            }
            case entries: {
                return AqlParser.entriesPhysicalFields;
            }
            case properties: {
                return AqlParser.propertiesPhysicalFields;
            }
            case statistics: {
                return AqlParser.statisticsPhysicalFields;
            }
            case artifacts: {
                return AqlParser.buildArtifactPhysicalFields;
            }
            case dependencies: {
                return AqlParser.buildDependenciesPhysicalFields;
            }
            case builds: {
                return AqlParser.buildPhysicalFields;
            }
            case modules: {
                return AqlParser.buildModulePhysicalFields;
            }
            case moduleProperties: {
                return AqlParser.buildModulePropertiesPhysicalFields;
            }
            case buildProperties: {
                return AqlParser.buildPropertiesPhysicalFields;
            }
            case buildPromotions: {
                return AqlParser.buildPromotionsPhysicalFields;
            }
            case releaseBundles: {
                return AqlParser.releaseBundlesPhysicalFields;
            }
            case releaseBundleFiles: {
                return AqlParser.releaseBundleFilesPhysicalFields;
            }
        }
        throw new UnsupportedOperationException("Unsupported domain :" + domain);
    }
}
