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

package org.artifactory.storage.db.security.itest.dao;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import org.artifactory.model.xstream.security.UserProperty;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.security.dao.UserPropertiesDao;
import org.artifactory.storage.db.security.entity.UserProp;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests {@link UserPropertiesDao}.
 *
 * @author Yossi Shaul
 */
@Test
@Deprecated
public class UserPropertiesDaoTest extends DbBaseTest {

    @Autowired
    private UserPropertiesDao dao;

    @BeforeClass
    public void setup() {
        importSql("/sql/user_properties.sql");
    }

    public void addPropertyToExistingUser() throws SQLException {
        dao.addUserPropertyById(1, "something", "good");    // user oferc
    }

    @Test(expectedExceptions = SQLIntegrityConstraintViolationException.class)
    public void addPropertyToNonExistingUser() throws SQLException {
        try {
            dao.addUserPropertyById(9911828, "wont", "work");
        } catch (PSQLException e) {
            assertThat(e.getMessage()).containsIgnoringCase("violates foreign key constraint");
            throw new SQLIntegrityConstraintViolationException(e);
        } catch (SQLServerException e) {
            assertThat(e.getMessage()).containsIgnoringCase("The INSERT statement conflicted with the FOREIGN KEY constraint");
            throw new SQLIntegrityConstraintViolationException(e);
        }
    }

    @Test(dependsOnMethods = "addPropertyToExistingUser")
    public void getUserProperties() throws SQLException {
        List<UserProperty> props = dao.getPropertiesForUser("oferc");
        assertThat(props).hasSize(1);
        assertEquals(props.get(0).getPropKey(), "something");
        assertEquals(props.get(0).getPropValue(), "good");
    }

    public void getAllPropertiesByKey() throws SQLException {
        dao.addUserPropertyById(3, "apiKey", "1234");
        dao.addUserPropertyById(4, "apiKey", "abcd");
        List<UserProp> props = dao.getAllPropertiesByKey("apiKey");
        assertThat(props).hasSize(2);
        UserProp prop1 = getById(props, 3);
        assertNotNull(prop1);
        assertThat(prop1.getPropKey()).isEqualTo("apiKey");
        assertThat(prop1.getPropVal()).isEqualTo("1234");
        UserProp prop2 = getById(props, 4);
        assertNotNull(prop2);
        assertThat(prop2.getPropKey()).isEqualTo("apiKey");
        assertThat(prop2.getPropVal()).isEqualTo("abcd");
    }

    public void getUserPropertiesNonExistingUser() throws SQLException {
        List<UserProperty> props = dao.getPropertiesForUser("nosuchuser");
        assertEquals(props.size(), 0);
    }

    private UserProp getById(List<UserProp> userProps, long id) {
        for (UserProp userProp : userProps) {
            if (userProp.getUserId() == id) {
                return userProp;
            }
        }
        return null;
    }
}
