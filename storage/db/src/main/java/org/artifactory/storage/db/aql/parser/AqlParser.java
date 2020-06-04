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

package org.artifactory.storage.db.aql.parser;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.AqlParserException;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.action.ActionPropertyKeysElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.action.ActionValueElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.action.DryRunElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.archive.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.build.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.build.artifact.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.build.dependency.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.build.module.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.build.promotion.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.build.property.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.item.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.property.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.releasebundle.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.releasebundlefile.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.statistics.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.language.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.language.action.*;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalEmptyElement;

import java.util.Set;

/**
 * Parse the Aql string query into parser result which is a list of parser elements
 * The parser does not have state therefore a single instance can be used on single JVM
 *
 * @author Gidi Shabat
 */
public class AqlParser {
    public static final String[] DELIMITERS = {"<=", ">=", "!=", " ", "<", ">", "(", ")", "[", "]", "{", "}", "=", "'", ".", ":", "\"", ":"};
    public static final String[] USED_KEYS = {"$mt", "$lt", "$eq", "and", "not", "or", "artifacts"};
    public static final DotElement dot = new DotElement();
    public static final CommaElement comma = new CommaElement();
    public static final StarElement star = new StarElement();
    public static final QuotedComparator quotedComparator = new QuotedComparator();
    public static final QuotedRelativeDateComparator quotedRelativeDateComparator = new QuotedRelativeDateComparator();
    public static final ComparatorElement comparator = new ComparatorElement();
    public static final RelativeDateComparatorElement relativeDateComparator = new RelativeDateComparatorElement();
    public static final QuotesElement quotes = new QuotesElement();
    public static final ColonElement colon = new ColonElement();
    public static final ValueNumberNullElement valueOrNumberOrNull = new ValueNumberNullElement();
    public static final ValueElement value = new ValueElement();
    public static final NumberElement number = new NumberElement();
    public static final NullElement nullElement = new NullElement();
    public static final OpenParenthesisElement openParenthesis = new OpenParenthesisElement();
    public static final CloseParenthesisElement closeParenthesis = new CloseParenthesisElement();
    public static final OpenCurlyBracketsElement openCurlyBrackets = new OpenCurlyBracketsElement();
    public static final CloseCurlyBracketsElement closedCurlyBrackets = new CloseCurlyBracketsElement();
    public static final OpenBracketsElement openBrackets = new OpenBracketsElement();
    public static final CloseBracketsElement closeBrackets = new CloseBracketsElement();
    public static final InternalEmptyElement empty = new InternalEmptyElement();
    public static final SortTypeElement sortType = new SortTypeElement();
    public static final LimitValueElement limitValue = new LimitValueElement();
    public static final OffsetValueElement offsetValue = new OffsetValueElement();
    public static final BooleanValueElement booleanValue = new BooleanValueElement();
    public static final DryRunValueElement dryRunValue = new DryRunValueElement();
    public static final ActionNewValueElement actionNewValue = new ActionNewValueElement();
    public static final ActionPropertyKeysValueElement actionPropertyKeysValue = new ActionPropertyKeysValueElement();
    public static final ActionPropertyKeysTailElement actionPropertyKeysTrail = new ActionPropertyKeysTailElement();

    public static final BuildPromotionsDomainsElement buildPromotionsDomains = new BuildPromotionsDomainsElement();
    public static final StatisticsDomainsElement statisticsDomains = new StatisticsDomainsElement();
    public static final BuildDomainsElement buildDomains = new BuildDomainsElement();
    public static final BuildPropertyDomainsElement buildPropertiesDomains = new BuildPropertyDomainsElement();
    public static final BuildModulePropertyDomainsElement buildModulePropertiesDomains = new BuildModulePropertyDomainsElement();
    public static final BuildModuleDomainsElement buildModuleDomains = new BuildModuleDomainsElement();
    public static final BuildDependenciesDomainsElement buildDependenciesDomains = new BuildDependenciesDomainsElement();
    public static final BuildArtifactDomainsElement buildArtifactDomains = new BuildArtifactDomainsElement();
    public static final PropertyDomainsElement propertiesDomains = new PropertyDomainsElement();
    public static final ArchiveEntriesDomainsElement entriesDomains = new ArchiveEntriesDomainsElement();
    public static final ArchiveDomainsElement archiveDomains = new ArchiveDomainsElement();
    public static final ItemDomainsElement itemDomains = new ItemDomainsElement();
    public static final ReleaseBundleDomainsElement releaseBundleDomains = new ReleaseBundleDomainsElement();
    public static final ReleaseBundleFileDomainsElement releaseBundleFileDomains = new ReleaseBundleFileDomainsElement();

    public static final BuildPromotionsStarElement buildPromotionsStar = new BuildPromotionsStarElement();
    public static final StatisticsStarElement statisticsStar = new StatisticsStarElement();
    public static final BuildStarElement buildStar = new BuildStarElement();
    public static final BuildPropertyStarElement buildPropertiesStar = new BuildPropertyStarElement();
    public static final BuildModulePropertyStarElement buildModulePropertiesStar = new BuildModulePropertyStarElement();
    public static final BuildModuleStarElement buildModuleStar = new BuildModuleStarElement();
    public static final BuildDependenciesStarElement buildDependenciesStar = new BuildDependenciesStarElement();
    public static final BuildArtifactStarElement buildArtifactStar = new BuildArtifactStarElement();
    public static final PropertyStarElement propertiesStar = new PropertyStarElement();
    public static final ArchiveEntriesStarElement entriesStar = new ArchiveEntriesStarElement();
    public static final ArchiveStarElement archiveStar = new ArchiveStarElement();
    public static final ItemStarElement itemStar = new ItemStarElement();
    public static final ReleaseBundleStarElement releaseBundlesStar = new ReleaseBundleStarElement();
    public static final ReleaseBundleFileStarElement releaseBundleFilesStar = new ReleaseBundleFileStarElement();

    public static final BuildPromotionsValuesElement buildPromotionsValues = new BuildPromotionsValuesElement();
    public static final StatisticsValuesElement statisticsValues = new StatisticsValuesElement();
    public static final BuildValuesElement buildValues = new BuildValuesElement();
    public static final BuildPropertyValuesElement buildPropertiesValues = new BuildPropertyValuesElement();
    public static final BuildModulePropertyValuesElement buildModulePropertiesValues = new BuildModulePropertyValuesElement();
    public static final BuildModuleValuesElement buildModuleValues = new BuildModuleValuesElement();
    public static final BuildDependenciesValuesElement buildDependenciesValues = new BuildDependenciesValuesElement();
    public static final BuildArtifactValuesElement buildArtifactValues = new BuildArtifactValuesElement();
    public static final PropertyValuesElement propertiesValues = new PropertyValuesElement();
    public static final ArchiveEntriesValuesElement entriesValues = new ArchiveEntriesValuesElement();
    public static final ArchiveValuesElement archiveValues = new ArchiveValuesElement();
    public static final ItemValuesElement itemValues = new ItemValuesElement();
    public static final ReleaseBundleValuesElement releaseBundlesValues = new ReleaseBundleValuesElement();
    public static final ReleaseBundleFileValuesElement releaseBundleFilesValues = new ReleaseBundleFileValuesElement();

    public static final BuildPromotionsPhysicalFieldsElement buildPromotionsPhysicalFields = new BuildPromotionsPhysicalFieldsElement();
    public static final StatisticsPhysicalFieldsElement statisticsPhysicalFields = new StatisticsPhysicalFieldsElement();
    public static final BuildPhysicalFieldsElement buildPhysicalFields = new BuildPhysicalFieldsElement();
    public static final BuildPropertyPhysicalFieldsElement buildPropertiesPhysicalFields = new BuildPropertyPhysicalFieldsElement();
    public static final BuildModulePropertyPhysicalFieldsElement buildModulePropertiesPhysicalFields = new BuildModulePropertyPhysicalFieldsElement();
    public static final BuildModulePhysicalFieldsElement buildModulePhysicalFields = new BuildModulePhysicalFieldsElement();
    public static final BuildDependenciesPhysicalFieldsElement buildDependenciesPhysicalFields = new BuildDependenciesPhysicalFieldsElement();
    public static final BuildArtifactPhysicalFieldsElement buildArtifactPhysicalFields = new BuildArtifactPhysicalFieldsElement();
    public static final PropertyPhysicalFieldsElement propertiesPhysicalFields = new PropertyPhysicalFieldsElement();
    public static final ArchiveEntryPhysicalFieldsElement entriesPhysicalFields = new ArchiveEntryPhysicalFieldsElement();
    public static final ArchivePhysicalFieldsElement archivePhysicalFields = new ArchivePhysicalFieldsElement();
    public static final ItemPhysicalFieldsElement itemPhysicalFields = new ItemPhysicalFieldsElement();
    public static final ReleaseBundlePhysicalFieldsElement releaseBundlesPhysicalFields = new ReleaseBundlePhysicalFieldsElement();
    public static final ReleaseBundleFilePhysicalFieldsElement releaseBundleFilesPhysicalFields = new ReleaseBundleFilePhysicalFieldsElement();

    public static final BuildPromotionsLogicalFieldsElement buildPromotionsLogicalFields = new BuildPromotionsLogicalFieldsElement();
    public static final StatisticsLogicalFieldsElement statisticsLogicalFields = new StatisticsLogicalFieldsElement();
    public static final BuildLogicalFieldsElement buildLogicalFields = new BuildLogicalFieldsElement();
    public static final BuildPropertyLogicalFieldsElement buildPropertiesLogicalFields = new BuildPropertyLogicalFieldsElement();
    public static final BuildModulePropertyLogicalFieldsElement buildModulePropertiesLogicalFields = new BuildModulePropertyLogicalFieldsElement();
    public static final BuildModuleLogicalFieldsElement buildModuleLogicalFields = new BuildModuleLogicalFieldsElement();
    public static final BuildDependenciesLogicalFieldsElement buildDependenciesLogicalFields = new BuildDependenciesLogicalFieldsElement();
    public static final BuildArtifactLogicalFieldsElement buildArtifactLogicalFields = new BuildArtifactLogicalFieldsElement();
    public static final PropertyLogicalFieldsElement propertiesLogicalFields = new PropertyLogicalFieldsElement();
    public static final ArchiveEntryLogicalFieldsElement entriesLogicalFields = new ArchiveEntryLogicalFieldsElement();
    public static final ArchiveLogicalFieldsElement archiveLogicalFields = new ArchiveLogicalFieldsElement();
    public static final ItemLogicalFieldsElement itemLogicalFields = new ItemLogicalFieldsElement();
    public static final ReleaseBundleLogicalFieldsElement releaseBundlesLogicalFields = new ReleaseBundleLogicalFieldsElement();
    public static final ReleaseBundleFileLogicalFieldsElement releaseBundleFilesLogicalFields = new ReleaseBundleFileLogicalFieldsElement();

    public static final OffsetElement offset = new OffsetElement();
    public static final LimitElement limit = new LimitElement();
    public static final RootElement root = new RootElement();

    public static final FindActionElement findAction = new FindActionElement();
    public static final DeleteActionElement deleteAction = new DeleteActionElement();
    public static final UpdateActionElement updateAction = new UpdateActionElement();
    public static final DryRunElement dryRun = new DryRunElement();
    public static final ActionValueElement actionValue = new ActionValueElement();
    public static final ActionPropertyKeysElement actionPropertyKeys = new ActionPropertyKeysElement();

    /*
     * Init once during the class initialisation.
     * All the parser instances will use the same parser elements instances
     */
    static {
        root.initialize();
    }

    /**
     * Initialize the parser process starting from the root element which represent the entire language
     *
     * @param query The AQL query string
     * @return Parsing result
     * @throws AqlParserException If query parsing fails
     */
    public ParserElementResultContainer parse(String query) {
        AqlParserContext parserContext = new AqlParserContext();
        ParserElementResultContainer[] parserElementResultContainers = root.peelOff(query, parserContext);
        for (ParserElementResultContainer parserElementResultContainer : parserElementResultContainers) {
            if (StringUtils.isBlank(parserElementResultContainer.getQueryRemainder())) {
                return parserElementResultContainer;
            }
        }
        String subQuery = parserContext.getQueryRemainder() != null ?
                parserContext.getQueryRemainder().trim() : query.trim();
        throw new AqlParserException(String.format("Failed to parse query: %s, it looks like there is syntax error near" +
                " the following sub-query: %s", query, subQuery));
    }

    /**
     * Initialize the parser process starting from the root element which represent the entire language and unlike the parse method
     * return the available possibilities to accept the next key word
     * This method is important for the new advance UI search.
     * <p/>
     * Examples:
     * 1. The String "items.find" will return "("
     * 2. The String "items.find(" will return {"(","\""}
     * 3. The String "items.find()" will return {"<empty>","."}
     * 3. The String "items.find()." will return {"sort","limit","include"}
     *
     * @param query The AQL query string
     * @return available possibilities to accept the next key word
     */
    public Set<String> predictNextKeyWord(String query) {
        AqlParserContext parserContext = new AqlParserContext();
        root.peelOff(query, parserContext);
        Set<String> result = Sets.newHashSet();
        for (ParserElement element : parserContext.getElements()) {
            for (String s : element.next()) {
                result.add(s);
}
        }
        System.out.println(result);
        return result;
    }
}


