package org.artifactory.storage.db.spring;

import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.jfrog.storage.DbType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

@Test
public class DbConfigFactoryTest {

    private DbConfigFactory factory = new DbConfigFactory();

    @Mock
    private BeanFactory beanFactory;
    @Mock
    private ArtifactoryDbProperties dbProperties;

    @BeforeMethod
    private void setup() {
        MockitoAnnotations.initMocks(this);
        factory.setBeanFactory(beanFactory);
    }

    public void testCreateUniqueLockDataSource() {
        when(beanFactory.getBean(ArtifactoryDbProperties.class)).thenReturn(dbProperties);
        when(dbProperties.getProperty(ArtifactoryDbProperties.Key.poolType)).thenReturn("tomcat-jdbc");
        when(dbProperties.getConnectionUrl()).thenReturn("jdbc:oracle:thin:@localhost:1521:xe");
        when(dbProperties.getDriverClass()).thenReturn("ORACLE.DRIVER");
        when(dbProperties.getUsername()).thenReturn("orcluser");
        when(dbProperties.getPassword()).thenReturn("orclpass");
        when(dbProperties.getLockingDbSpecificType()).thenReturn(Optional.empty());
        when(dbProperties.getDbType()).thenReturn(DbType.ORACLE);
        when(dbProperties.getProperty("validationQuery", "SELECT 1 FROM DUAL")).thenReturn("query");
        factory.createUniqueLockDataSource();
        verify(dbProperties, times(1)).getProperty(ArtifactoryDbProperties.Key.poolType);
        verify(dbProperties, times(2)).getConnectionUrl();
        verify(dbProperties, times(1)).getDriverClass();
        verify(dbProperties, times(1)).getUsername();
        verify(dbProperties, times(1)).getPassword();
        verify(dbProperties, times(1)).getProperty("validationQuery", "SELECT 1 FROM DUAL");
    }

    public void testCreateUniqueLockDataSourceOnDedicatedDb() {
        testUniqueLockDatasourceOnDifferentDb();
    }

    public void testCreateUniqueLockDataSourceOnDedicatedDbWhileMainDbIsHikari() {
        // db.properties type is configured as hikari
        when(dbProperties.getProperty(ArtifactoryDbProperties.Key.poolType)).thenReturn("tomcat-jdbc");
        testUniqueLockDatasourceOnDifferentDb();
    }

    private void testUniqueLockDatasourceOnDifferentDb() {
        when(beanFactory.getBean(ArtifactoryDbProperties.class)).thenReturn(dbProperties);
        when(dbProperties.getLockingDbConnectionUrl()).thenReturn("jdbc:oracle:thin:@localhost:1521:xe");
        when(dbProperties.getLockingDbDriverClass()).thenReturn("ORACLE.DRIVER");
        when(dbProperties.getLockingDbUsername()).thenReturn("orcluser");
        when(dbProperties.getLockingDbPassword()).thenReturn("orclpass");
        when(dbProperties.getLockingDbSpecificType()).thenReturn(Optional.of(DbType.ORACLE));
        when(dbProperties.getProperty("lockingdb.validationQuery", "SELECT 1 FROM DUAL")).thenReturn("query");
        factory.createUniqueLockDataSource();
        verify(dbProperties, times(1)).getLockingDbConnectionUrl();
        verify(dbProperties, times(1)).getLockingDbDriverClass();
        verify(dbProperties, times(1)).getLockingDbUsername();
        verify(dbProperties, times(1)).getLockingDbPassword();
        verify(dbProperties, times(1)).getProperty("lockingdb.validationQuery", "SELECT 1 FROM DUAL");
    }
}