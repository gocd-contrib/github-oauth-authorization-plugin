/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.authorization.github;

import cd.go.authorization.github.models.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class GitHubAuthorizerTest {

    private GitHubAuthorizer authorizer;
    private MembershipChecker membershipChecker;
    private LoggedInUserInfo loggedInUserInfo;
    private AuthConfig authConfig;

    @Before
    public void setUp() throws Exception {
        membershipChecker = mock(MembershipChecker.class);
        loggedInUserInfo = mock(LoggedInUserInfo.class);
        authConfig = mock(AuthConfig.class);

        when(loggedInUserInfo.getUser()).thenReturn(new User("bob", "Bob", null));

        authorizer = new GitHubAuthorizer(membershipChecker);
    }

    @Test
    public void shouldReturnEmptyListIfNoRoleConfiguredForGivenAuthConfig() throws Exception {
        final List<String> assignedRoles = authorizer.authorize(loggedInUserInfo, authConfig, Collections.emptyList());

        assertThat(assignedRoles, hasSize(0));
        verifyZeroInteractions(authConfig);
        verifyZeroInteractions(membershipChecker);
    }

    @Test
    public void shouldAssignRoleIfUsernameListContainsUsernameOfTheLoggedInUser() throws Exception {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.users()).thenReturn(singletonList("bob"));

        final List<String> assignedRoles = authorizer.authorize(loggedInUserInfo, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(1));
        assertThat(assignedRoles, contains("admin"));
    }

    @Test
    public void shouldNotAssignRoleIfUsernameOfTheLoggedInUserIsNotListedInUsernameList() throws Exception {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.users()).thenReturn(singletonList("alice"));

        final List<String> assignedRoles = authorizer.authorize(loggedInUserInfo, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(0));
    }

    @Test
    public void shouldAssignRoleIfUserIsAMemberOfAtLeastOneOrganization() throws Exception {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.organizations()).thenReturn(singletonList("organization-1"));
        when(membershipChecker.isAMemberOfAtLeastOneOrganization(loggedInUserInfo, authConfig, roleConfiguration.organizations())).thenReturn(true);

        final List<String> assignedRoles = authorizer.authorize(loggedInUserInfo, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(1));
        assertThat(assignedRoles, contains("admin"));
    }

    @Test
    public void shouldNotAssignRoleIfUserIsNotMemberOfAnyOrganization() throws Exception {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.organizations()).thenReturn(singletonList("organization-1"));
        when(membershipChecker.isAMemberOfAtLeastOneOrganization(loggedInUserInfo, authConfig, roleConfiguration.organizations())).thenReturn(false);

        final List<String> assignedRoles = authorizer.authorize(loggedInUserInfo, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(0));
    }

    @Test
    public void shouldAssignRoleIfUserIsAMemberOfAtLeastOneOrganizationTeam() throws Exception {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.teams()).thenReturn(singletonMap("organization-1", singletonList("team-1")));
        when(membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(loggedInUserInfo, authConfig, roleConfiguration.teams())).thenReturn(true);

        final List<String> assignedRoles = authorizer.authorize(loggedInUserInfo, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(1));
        assertThat(assignedRoles, contains("admin"));
    }

    @Test
    public void shouldNotAssignRoleIfUserIsNotMemberOfAnyOrganizationTeam() throws Exception {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.teams()).thenReturn(singletonMap("organization-1", singletonList("team-1")));
        when(membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(loggedInUserInfo, authConfig, roleConfiguration.teams())).thenReturn(false);

        final List<String> assignedRoles = authorizer.authorize(loggedInUserInfo, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(0));
    }
}