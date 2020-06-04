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
public class DynamicLogicalField extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        switch (domain) {
            case items:
                return AqlParser.itemLogicalFields;
            case archives:
                return AqlParser.archiveLogicalFields;
            case entries:
                return AqlParser.entriesLogicalFields;
            case properties:
                return AqlParser.propertiesLogicalFields;
            case statistics:
                return AqlParser.statisticsLogicalFields;
            case artifacts:
                return AqlParser.buildArtifactLogicalFields;
            case dependencies:
                return AqlParser.buildDependenciesLogicalFields;
            case builds:
                return AqlParser.buildLogicalFields;
            case modules:
                return AqlParser.buildModuleLogicalFields;
            case moduleProperties:
                return AqlParser.buildModulePropertiesLogicalFields;
            case buildProperties:
                return AqlParser.buildPropertiesLogicalFields;
            case buildPromotions:
                return AqlParser.buildPromotionsLogicalFields;
            case releaseBundles:
                return AqlParser.releaseBundlesLogicalFields;
            case releaseBundleFiles:
                return AqlParser.releaseBundleFilesLogicalFields;
        }
        throw new UnsupportedOperationException("Unsupported domain :" + domain);
    }
}
