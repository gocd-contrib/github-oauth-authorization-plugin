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

import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubRoleConfiguration;
import cd.go.authorization.github.models.Role;
import cd.go.authorization.github.models.User;
import cd.go.authorization.github.providermanager.GitHubProviderManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GitHubAuthorizerTest {

    private GitHubProviderManager providerManager;
    private GitHubProvider provider;
    private GitHubAuthorizer authorizer;

    @Before
    public void setUp() throws Exception {
        providerManager = mock(GitHubProviderManager.class);
        provider = mock(GitHubProvider.class);

        when(providerManager.getGitHubProvider(any(AuthConfig.class))).thenReturn(provider);

        authorizer = new GitHubAuthorizer(providerManager);
    }

    @Test
    public void authorize_shouldReturnEmptyListIfRoleListIsEmpty() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final AuthConfig authConfig = mock(AuthConfig.class);

        final List<String> assignedRoles = authorizer.authorize(user, authConfig, Collections.emptyList());

        assertThat(assignedRoles, hasSize(0));
        verifyZeroInteractions(providerManager);
    }

    @Test
    public void authorize_shouldAssignRoleIfUserBelongsToRoleUsers() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final AuthConfig authConfig = mock(AuthConfig.class);
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.users()).thenReturn(singletonList("bob"));

        final List<String> assignedRoles = authorizer.authorize(user, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(1));
        assertThat(assignedRoles, contains("admin"));
    }

    @Test
    public void authorize_shouldNotAssignRoleIfUserIsNotBelongsToRoleUsers() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final AuthConfig authConfig = mock(AuthConfig.class);
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.users()).thenReturn(singletonList("alice"));

        final List<String> assignedRoles = authorizer.authorize(user, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(0));
    }

    @Test
    public void authorize_shouldAssignRoleIfUserIsAMemberOfAtLeastOneOrganization() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final AuthConfig authConfig = mock(AuthConfig.class);
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.organizations()).thenReturn(singletonList("organization-1"));
        when(provider.isAMemberOfAtLeastOneOrganization(user, roleConfiguration.organizations())).thenReturn(true);

        final List<String> assignedRoles = authorizer.authorize(user, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(1));
        assertThat(assignedRoles, contains("admin"));
    }

    @Test
    public void authorize_shouldNotAssignRoleIfUserIsNotMemberOfAnyOrganization() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final AuthConfig authConfig = mock(AuthConfig.class);
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.organizations()).thenReturn(singletonList("organization-1"));
        when(provider.isAMemberOfAtLeastOneOrganization(user, roleConfiguration.organizations())).thenReturn(false);

        final List<String> assignedRoles = authorizer.authorize(user, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(0));
    }

    @Test
    public void authorize_shouldAssignRoleIfUserIsAMemberOfAtLeastOneOrganizationTeam() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final AuthConfig authConfig = mock(AuthConfig.class);
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.teams()).thenReturn(singletonMap("organization-1", singletonList("team-1")));
        when(provider.isAMemberOfAtLeastOneTeamOfOrganization(user, roleConfiguration.teams())).thenReturn(true);

        final List<String> assignedRoles = authorizer.authorize(user, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(1));
        assertThat(assignedRoles, contains("admin"));
    }

    @Test
    public void authorize_shouldNotAssignRoleIfUserIsNotMemberOfAnyOrganizationTeam() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final AuthConfig authConfig = mock(AuthConfig.class);
        final Role role = mock(Role.class);
        final GitHubRoleConfiguration roleConfiguration = mock(GitHubRoleConfiguration.class);

        when(role.name()).thenReturn("admin");
        when(role.roleConfiguration()).thenReturn(roleConfiguration);
        when(roleConfiguration.teams()).thenReturn(singletonMap("organization-1", singletonList("team-1")));
        when(provider.isAMemberOfAtLeastOneTeamOfOrganization(user, roleConfiguration.teams())).thenReturn(false);

        final List<String> assignedRoles = authorizer.authorize(user, authConfig, singletonList(role));

        assertThat(assignedRoles, hasSize(0));
    }
}