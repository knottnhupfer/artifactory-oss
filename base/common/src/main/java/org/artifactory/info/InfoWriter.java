package org.artifactory.info;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.artifactory.common.ConstantValues;
import org.jfrog.support.common.core.collectors.system.info.BasePropInfoGroup;
import org.jfrog.support.common.core.collectors.system.info.CommonInfoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * @author Tamir Hadad
 */
public class InfoWriter extends CommonInfoWriter {
    private static final Logger log = LoggerFactory.getLogger(InfoWriter.class);
    /**
     * A list of property keys for which the value should be masked
     */
    private static final Set<String> maskedKeys = Sets.newHashSet(
            ConstantValues.s3backupAccountId.getPropertyName(),
            ConstantValues.s3backupAccountSecretKey.getPropertyName()
    );

    protected static Set<String> getMaskedKeys() {
        return maskedKeys;
    }

    @Override
    protected List<Class<? extends BasePropInfoGroup>> getGroups() {
        return ImmutableList.<Class<? extends BasePropInfoGroup>>builder().addAll(super.getGroups())
                .add(ArtifactoryPropInfo.class, HaPropInfo.class).build();
    }
    
    protected CommonInfoWriter createInfoWriter() {
        return new InfoWriter();
    }

    /**
     * Dumps the info from all the groups in the enum to the log
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void writeInfo() throws IllegalAccessException, InstantiationException {
        if (log.isInfoEnabled()) {
            log.info(getInfo());
        }
    }
}
