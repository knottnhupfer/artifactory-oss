package org.artifactory.storage.db.event.service.metadata.mapper;

import org.apache.commons.lang.StringUtils;
import org.jfrog.metadata.client.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 */
@Mapper(componentModel = "spring")
@Named("MetadataEntityTranslator")
class MetadataEntityTranslator {

    @Named("LicensesTranslator")
    @Nullable
    List<MetadataLicense> licensesNamesToMdsLicense(List<String> licenses) {
        if (!licenses.isEmpty()) {
            return licenses.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(licenseName -> new MetadataLicense(licenseName, null, null, 0, null))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Named("UserPropertiesTranslator")
    List<MetadataUserProperty> propertyMapToMdsUserPropertyList(Map<String, Set<String>> propertyMap) {
        if (!propertyMap.isEmpty()) {
            return propertyMap.entrySet().stream()
                    .map(entry -> new MetadataUserProperty(entry.getKey(), String.join(",", entry.getValue())))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Named("QualifiersTranslator")
    List<MetadataQualifierEntity> createMdsQualifiers(Map<String, String> qualifiersMap) {
        if (!qualifiersMap.isEmpty()) {
            return qualifiersMap.entrySet().stream()
                    .map(entry -> new MetadataQualifierEntity(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Named("TagsTranslator")
    Set<MetadataVersionTagEntity> createMdsTags(Set<String> tags) {
        if (!tags.isEmpty()) {
            return tags.stream()
                    .map(MetadataVersionTagEntity::new)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
