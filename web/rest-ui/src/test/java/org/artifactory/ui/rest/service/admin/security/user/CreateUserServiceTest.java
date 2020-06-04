package org.artifactory.ui.rest.service.admin.security.user;

import com.google.common.collect.ImmutableSet;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.model.xstream.security.GroupImpl;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.ui.rest.model.admin.security.group.UserGroup;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 */
@Test
public class CreateUserServiceTest {
    @Mock
    ArtifactoryRestRequest<User> requestMock;

    @Mock
    UserGroupService userGroupService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDefaultGroupAddedToNewUser() {
        User user = new User();
        user.setName("jon");

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupName("readers");
        user.setUserGroups(ImmutableSet.of(userGroup));

        CreateUserService<User> createUserService = new CreateUserService<>(null, userGroupService);
        when(requestMock.getImodel()).thenReturn(user);
        GroupImpl readersGroup = new GroupImpl();
        readersGroup.setGroupName("readers");

        ArgumentCaptor<MutableUserInfo> captor = ArgumentCaptor.forClass(MutableUserInfo.class);
        createUserService.execute(requestMock, mock(RestResponse.class));

        verify(userGroupService).createUser(captor.capture());

        MutableUserInfo createdUser = captor.getValue();
        assertNotNull(createdUser);
        assertEquals(1, createdUser.getGroups().size());
        assertTrue(createdUser.getGroups().stream().anyMatch(group -> "readers".equals(group.getGroupName())));
    }

    @Test
    public void testDefaultGroupRemovedInUINotAddedToNewUser() {
        User user = new User();
        user.setName("jon");

        user.setUserGroups(ImmutableSet.of());

        CreateUserService<User> createUserService = new CreateUserService<>(null, userGroupService);
        when(requestMock.getImodel()).thenReturn(user);
        GroupImpl readersGroup = new GroupImpl();
        readersGroup.setGroupName("readers");

        ArgumentCaptor<MutableUserInfo> captor = ArgumentCaptor.forClass(MutableUserInfo.class);
        createUserService.execute(requestMock, mock(RestResponse.class));

        verify(userGroupService).createUser(captor.capture());

        MutableUserInfo createdUser = captor.getValue();
        assertNotNull(createdUser);
        assertEquals(0, createdUser.getGroups().size());
    }
}