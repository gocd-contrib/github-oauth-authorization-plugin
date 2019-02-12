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
import org.kohsuke.github.GHUser;

import java.io.IOException;
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
    private GHUser ghUser;
    private AuthConfig authConfig;

    @Before
    public void setUp() {
        membershipChecker = mock(MembershipChecker.class);
        ghUser = mock(GHUser.class);
        authConfig = mock(AuthConfig.class);
        when(ghUser.getLogin()).thenReturn("bob");

        authorizer = new GitHubAuthorizer(membershipChecker);
    }

    @Test
    public void shouldReturnEmptyListIfNoRoleConfiguredForGivenAuthConfig() throws IOException {
        final List<String> assignedRoles = authorizer.authorize(ghUser, authConfig, Collections.emptyList());

        assertThat(assignedRoles, hasSize(0));
        verifyZeroInteractions(authConfig);
        verifyZeroInteractions(membershipChecker);
    }

    @Test
    public void shouldAssignRoleWhenUsersListContainsUsernameOfTheGivenUser() throws IOException {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.users()).thenReturn(singletonList("bob"));

        final List<String> assignedRoles = authorizer.authorize(ghUser, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(1));
        assertThat(assignedRoles, contains("admin"));
    }

    @Test
    public void shouldNotAssignRoleWhenUsersListDoesNotContainsTheGivenUser() throws IOException {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.users()).thenReturn(singletonList("alice"));

        final List<String> assignedRoles = authorizer.authorize(ghUser, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(0));
    }

    @Test
    public void shouldAssignRoleIfUserIsAMemberOfAtLeastOneOrganization() throws IOException {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.organizations()).thenReturn(singletonList("organization-1"));
        when(membershipChecker.isAMemberOfAtLeastOneOrganization(ghUser, authConfig, roleConfiguration.organizations())).thenReturn(true);

        final List<String> assignedRoles = authorizer.authorize(ghUser, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(1));
        assertThat(assignedRoles, contains("admin"));
    }

    @Test
    public void shouldNotAssignRoleIfUserIsNotMemberOfAnyOrganization() throws IOException {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.organizations()).thenReturn(singletonList("organization-1"));
        when(membershipChecker.isAMemberOfAtLeastOneOrganization(ghUser, authConfig, roleConfiguration.organizations())).thenReturn(false);

        final List<String> assignedRoles = authorizer.authorize(ghUser, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(0));
    }

    @Test
    public void shouldAssignRoleIfUserIsAMemberOfAtLeastOneOrganizationTeam() throws IOException {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.teams()).thenReturn(singletonMap("organization-1", singletonList("team-1")));
        when(membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(ghUser, authConfig, roleConfiguration.teams())).thenReturn(true);

        final List<String> assignedRoles = authorizer.authorize(ghUser, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(1));
        assertThat(assignedRoles, contains("admin"));
    }

    @Test
    public void shouldNotAssignRoleIfUserIsNotMemberOfAnyOrganizationTeam() throws IOException {
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.teams()).thenReturn(singletonMap("organization-1", singletonList("team-1")));
        when(membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(ghUser, authConfig, roleConfiguration.teams())).thenReturn(false);

        final List<String> assignedRoles = authorizer.authorize(ghUser, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(0));
    }
}