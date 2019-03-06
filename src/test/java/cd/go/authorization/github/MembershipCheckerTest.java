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
import cd.go.authorization.github.models.GitHubConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MembershipCheckerTest {

    private GitHub gitHub;
    private MembershipChecker membershipChecker;
    private AuthConfig authConfig;
    private GitHubConfiguration gitHubConfiguration;
    private GHUser ghUser;

    @Before
    public void setUp() throws IOException {
        gitHub = mock(GitHub.class);
        authConfig = mock(AuthConfig.class);
        gitHubConfiguration = mock(GitHubConfiguration.class);
        ghUser = mock(GHUser.class);
        final GitHubClientBuilder clientBuilder = mock(GitHubClientBuilder.class);

        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);
        when(clientBuilder.from(gitHubConfiguration)).thenReturn(gitHub);

        membershipChecker = new MembershipChecker(clientBuilder);
    }

    @Test
    public void shouldCheckUserMembershipUsingPersonalAccessToken_andReturnTrueIfUserIsAMemberOfAtLeastOneOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);

        when(organization.getName()).thenReturn("organization-foo");
        when(gitHub.getOrganization("organization-foo")).thenReturn(organization);
        when(organization.hasMember(ghUser)).thenReturn(true);

        final boolean aMemberOfAtLeastOneOrganization = membershipChecker.isAMemberOfAtLeastOneOrganization(ghUser, authConfig, asList("organization-foo", "organization-bar"));

        assertTrue(aMemberOfAtLeastOneOrganization);
    }

    @Test
    public void shouldCheckUserMembershipUsingPersonalAccessToken_andReturnFalseIfUserIsNotAMemberOfAnyOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);

        when(organization.getName()).thenReturn("organization-baz");
        when(gitHub.getOrganization("organization-foo")).thenReturn(organization);
        when(organization.hasMember(ghUser)).thenReturn(false);

        final boolean aMemberOfAtLeastOneOrganization = membershipChecker.isAMemberOfAtLeastOneOrganization(ghUser, authConfig, asList("organization-foo", "organization-bar"));

        assertFalse(aMemberOfAtLeastOneOrganization);
    }

    @Test
    public void shouldCheckUserMembershipUsingPersonalAccessToken_andReturnTrueIfUserIsAMemberOfAtLeastOneTeamOfOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);
        final GHTeam team = mock(GHTeam.class);

        when(team.getName()).thenReturn("TeamX");
        when(organization.getName()).thenReturn("organization-foo");
        when(organization.getTeams()).thenReturn(singletonMap("TeamX", team));
        when(gitHub.getOrganization("organization-foo")).thenReturn(organization);
        when(team.hasMember(ghUser)).thenReturn(true);

        final boolean aMemberOfAtLeastOneTeamOfOrganization = membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(ghUser, authConfig, singletonMap("organization-foo", asList("teamx")));

        assertTrue(aMemberOfAtLeastOneTeamOfOrganization);
    }

    @Test
    public void shouldCheckUserMembershipUsingPersonalAccessToken_andReturnFalseIfUserIsNotAMemberOfAnyTeamOfOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);
        final GHTeam teamX = mock(GHTeam.class);
        final GHTeam teamY = mock(GHTeam.class);

        final HashMap<String, GHTeam> teams = new HashMap<>();
        teams.put("TeamX", teamX);
        teams.put("TeamY", teamY);

        when(organization.getName()).thenReturn("organization-foo");
        when(organization.getTeams()).thenReturn(teams);
        when(gitHub.getOrganization("organization-foo")).thenReturn(organization);

        when(teamX.getName()).thenReturn("TeamX");
        when(teamX.hasMember(ghUser)).thenReturn(false);

        when(teamY.getName()).thenReturn("TeamY");
        when(teamX.hasMember(ghUser)).thenReturn(true);;

        final boolean aMemberOfAtLeastOneTeamOfOrganization = membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(ghUser, authConfig, singletonMap("organization-foo", asList("TeamX")));

        assertFalse(aMemberOfAtLeastOneTeamOfOrganization);
    }
}