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

package org.artifactory.storage.db.itest.spring;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.storage.db.DbMetaData;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.InternalDbService;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.jfrog.storage.DbType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Date: 8/5/13 10:56 AM
 *
 * @author freds
 */
@Configuration
public class DbUpgradeTestConfigFactory implements BeanFactoryAware {

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    }

    @Bean
    @Primary
    public DbService createDummyDbService() {
        return new DummyDbService();
    }

    @Bean
    public InternalDbService createDummyInternalDbService() {
        return new DummyDbService();
    }
}

class DummyDbService implements InternalDbService {
    @Override
    public long nextId() {
        return 0;
    }

    @Override
    public DbType getDatabaseType() {
        return DbType.DERBY;
    }

    @Override
    public DbMetaData getDbMetaData() {
        return null;
    }

    @Override
    public void compressDerbyDb(BasicStatusHolder statusHolder) {
        // Nothing
    }

    @Override
    public <T> T invokeInTransaction(String transactionName, Callable<T> execute) {
        return null;
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        // Nothing
    }

    @Override
    public void init() {
        // Nothing
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        // Nothing
    }

    @Override
    public void destroy() {
        // Nothing
    }

    @Override
    public void initDb() {

    }

    @Override
    public boolean verifySha256State()  {
        return true;
    }

    @Override
    public boolean isSha256Ready() {
        return true;
    }

    @Override
    public boolean isUniqueRepoPathChecksumReady() {
        return true;
    }

    @Override
    public void verifyMigrations() {

    }

    @Override
    public boolean verifyUniqueRepoPathChecksumState() {
        return true;
    }
}
