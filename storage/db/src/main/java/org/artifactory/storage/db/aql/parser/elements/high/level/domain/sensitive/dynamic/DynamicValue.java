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
public class DynamicValue extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        switch (domain) {
            case items: 
                return AqlParser.itemValues;
            case archives:
                return AqlParser.archiveValues;
            case entries:
                return AqlParser.entriesValues;
            case properties:
                return AqlParser.propertiesValues;
            case statistics:
                return AqlParser.statisticsValues;
            case artifacts:
                return AqlParser.buildArtifactValues;
            case dependencies:
                return AqlParser.buildDependenciesValues;
            case builds:
                return AqlParser.buildValues;
            case modules:
                return AqlParser.buildModuleValues;
            case moduleProperties:
                return AqlParser.buildModulePropertiesValues;
            case buildPromotions:
                return AqlParser.buildPromotionsValues;
            case buildProperties:
                return AqlParser.buildPropertiesValues;
            case releaseBundles:
                return AqlParser.releaseBundlesValues;
            case releaseBundleFiles:
                return AqlParser.releaseBundleFilesValues;
        }
        throw new UnsupportedOperationException("Unsupported domain :" + domain);
    }
}
