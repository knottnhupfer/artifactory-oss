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

package org.artifactory.storage.db.bundle.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseBundleModel;
import org.artifactory.bundle.BundleTransactionStatus;
import org.artifactory.bundle.BundleType;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static org.jfrog.common.ArgUtils.*;

/**
 * @author Tomer Mayost
 */
@Data
@NoArgsConstructor
public class DBArtifactsBundle {
    long id;
    String name;
    String version;
    BundleTransactionStatus status;
    DateTime dateCreated;
    String signature;
    BundleType type;
    String storingRepo;

    private static final Pattern pattern = Pattern.compile("^[a-zA-Z\\d]+[a-zA-Z\\d\\-_:.]*$");
    private static final String INVALID_PATTERN_MESSAGE = " can only contain letters, numbers and the following characters: .-_:";

    public static DBArtifactsBundle buildFrom(@Nonnull ReleaseBundleModel releaseBundleModel) {
        DBArtifactsBundle artifactsBundle = new DBArtifactsBundle();
        artifactsBundle.setName(releaseBundleModel.getName());
        artifactsBundle.setVersion(releaseBundleModel.getVersion());
        artifactsBundle.setDateCreated(parseDateCreated(releaseBundleModel.getCreated()));
        artifactsBundle.setStatus(releaseBundleModel.getStatus() == null ? BundleTransactionStatus.INPROGRESS :
                BundleTransactionStatus.valueOf(releaseBundleModel.getStatus()));
        artifactsBundle.setSignature(releaseBundleModel.getSignature());
        artifactsBundle.setType(releaseBundleModel.getType() == null ? BundleType.TARGET :
                BundleType.valueOf(releaseBundleModel.getType()));
        artifactsBundle.setStoringRepo(releaseBundleModel.getStoringRepo());
        artifactsBundle.validate();
        return artifactsBundle;

    }

    private static DateTime parseDateCreated(@Nonnull String date) {
        return ISODateTimeFormat.dateTime().withZoneUTC().parseDateTime(date);
    }

    public void validate() {
        validate(name, version, dateCreated, signature);
    }

    public void validate(String name, String version, DateTime dateCreated, String signature) {
        this.name = requireMatches(name, pattern, "Bundle name" + INVALID_PATTERN_MESSAGE);
        this.version = requireMatches(version, pattern, "Bundle version" + INVALID_PATTERN_MESSAGE);
        this.dateCreated = requireNonNull(dateCreated, "Date created must not be null");
        this.signature = requireNonBlank(signature, "Signature is mandatory");
    }
}
