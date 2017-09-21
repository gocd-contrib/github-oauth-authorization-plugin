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
import cd.go.authorization.github.models.LoggedInUserInfo;
import cd.go.authorization.github.models.User;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;

import java.util.HashMap;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MembershipCheckerTest {

    private GitHub gitHub;
    private MembershipChecker membershipChecker;
    private LoggedInUserInfo loggedInUserInfo;
    private AuthConfig authConfig;
    private GitHubConfiguration gitHubConfiguration;

    @Before
    public void setUp() throws Exception {
        gitHub = mock(GitHub.class);
        loggedInUserInfo = mock(LoggedInUserInfo.class);
        authConfig = mock(AuthConfig.class);
        gitHubConfiguration = mock(GitHubConfiguration.class);

        when(loggedInUserInfo.getUser()).thenReturn(new User("bford", "Bob", "bford@example.com"));
        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);

        membershipChecker = new MembershipChecker();
    }

    @Test
    public void shouldCheckUserMembershipUsingPersonalAccessToken_andReturnTrueIfUserIsAMemberOfAtLeastOneOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);

        when(organization.getName()).thenReturn("organization-foo");
        when(gitHubConfiguration.gitHubClient()).thenReturn(gitHub);
        when(gitHubConfiguration.authorizeUsingPersonalAccessToken()).thenReturn(true);
        when(gitHub.getOrganization("organization-foo")).thenReturn(organization);
        when(organization.hasMember(loggedInUserInfo.getGitHubUser())).thenReturn(true);

        final boolean aMemberOfAtLeastOneOrganization = membershipChecker.isAMemberOfAtLeastOneOrganization(loggedInUserInfo, authConfig, asList("organization-foo", "organization-bar"));

        assertTrue(aMemberOfAtLeastOneOrganization);
    }

    @Test
    public void shouldCheckUserMembershipUsingPersonalAccessToken_andReturnFalseIfUserIsNotAMemberOfAnyOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);

        when(organization.getName()).thenReturn("organization-baz");
        when(gitHubConfiguration.gitHubClient()).thenReturn(gitHub);
        when(gitHubConfiguration.authorizeUsingPersonalAccessToken()).thenReturn(true);
        when(gitHub.getOrganization("organization-foo")).thenReturn(organization);
        when(organization.hasMember(loggedInUserInfo.getGitHubUser())).thenReturn(false);

        final boolean aMemberOfAtLeastOneOrganization = membershipChecker.isAMemberOfAtLeastOneOrganization(loggedInUserInfo, authConfig, asList("organization-foo", "organization-bar"));

        assertFalse(aMemberOfAtLeastOneOrganization);
    }


    @Test
    public void shouldCheckUserMembershipUsingUsersAccessToken_andReturnTrueIfUserIsAMemberOfAtLeastOneOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);

        when(gitHub.getMyOrganizations()).thenReturn(singletonMap("organization-foo", organization));
        when(gitHubConfiguration.authorizeUsingPersonalAccessToken()).thenReturn(false);
        when(loggedInUserInfo.getGitHub()).thenReturn(gitHub);

        final boolean aMemberOfAtLeastOneOrganization = membershipChecker.isAMemberOfAtLeastOneOrganization(loggedInUserInfo, authConfig, asList("organization-foo", "organization-bar"));

        assertTrue(aMemberOfAtLeastOneOrganization);
    }

    @Test
    public void shouldCheckUserMembershipUsingUsersAccessToken_andReturnFalseIfUserIsNotAMemberOfAnyOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);

        when(gitHub.getMyOrganizations()).thenReturn(singletonMap("organization-baz", organization));
        when(gitHubConfiguration.authorizeUsingPersonalAccessToken()).thenReturn(false);
        when(loggedInUserInfo.getGitHub()).thenReturn(gitHub);

        final boolean aMemberOfAtLeastOneOrganization = membershipChecker.isAMemberOfAtLeastOneOrganization(loggedInUserInfo, authConfig, asList("organization-foo", "organization-bar"));

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
        when(gitHubConfiguration.gitHubClient()).thenReturn(gitHub);
        when(team.hasMember(loggedInUserInfo.getGitHubUser())).thenReturn(true);
        when(gitHubConfiguration.authorizeUsingPersonalAccessToken()).thenReturn(true);

        final boolean aMemberOfAtLeastOneTeamOfOrganization = membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(loggedInUserInfo, authConfig, singletonMap("organization-foo", asList("teamx")));

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
        when(teamX.hasMember(loggedInUserInfo.getGitHubUser())).thenReturn(false);

        when(teamY.getName()).thenReturn("TeamY");
        when(teamX.hasMember(loggedInUserInfo.getGitHubUser())).thenReturn(true);

        when(gitHubConfiguration.gitHubClient()).thenReturn(gitHub);
        when(gitHubConfiguration.authorizeUsingPersonalAccessToken()).thenReturn(true);

        final boolean aMemberOfAtLeastOneTeamOfOrganization = membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(loggedInUserInfo, authConfig, singletonMap("organization-foo", asList("TeamX")));

        assertFalse(aMemberOfAtLeastOneTeamOfOrganization);
    }

    @Test
    public void shouldCheckUserMembershipUsingUsersAccessToken_andReturnTrueIfUserIsAMemberOfAtLeastOneTeamOfOrganization() throws Exception {
        final GHTeam team = mock(GHTeam.class);

        when(team.getName()).thenReturn("TeamX");
        when(gitHub.getMyTeams()).thenReturn(singletonMap("organization-foo", singleton(team)));
        when(loggedInUserInfo.getGitHub()).thenReturn(gitHub);
        when(team.hasMember(loggedInUserInfo.getGitHubUser())).thenReturn(true);
        when(gitHubConfiguration.authorizeUsingPersonalAccessToken()).thenReturn(false);

        final boolean aMemberOfAtLeastOneTeamOfOrganization = membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(loggedInUserInfo, authConfig, singletonMap("organization-foo", asList("teamx")));

        assertTrue(aMemberOfAtLeastOneTeamOfOrganization);
    }

    @Test
    public void shouldCheckUserMembershipUsingUsersAccessToken_andReturnFalseIfUserIsNotAMemberOfAnyTeamOfOrganization() throws Exception {
        final GHTeam teamX = mock(GHTeam.class);
        final GHTeam teamY = mock(GHTeam.class);

        when(teamX.getName()).thenReturn("TeamX");
        when(teamX.hasMember(loggedInUserInfo.getGitHubUser())).thenReturn(false);

        when(teamX.getName()).thenReturn("TeamY");
        when(teamX.hasMember(loggedInUserInfo.getGitHubUser())).thenReturn(true);

        when(gitHub.getMyTeams()).thenReturn(singletonMap("organization-foo", new HashSet<>(asList(teamX, teamY))));
        when(loggedInUserInfo.getGitHub()).thenReturn(gitHub);
        when(gitHubConfiguration.authorizeUsingPersonalAccessToken()).thenReturn(false);

        final boolean aMemberOfAtLeastOneTeamOfOrganization = membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(loggedInUserInfo, authConfig, singletonMap("organization-foo", asList("TeamX")));

        assertFalse(aMemberOfAtLeastOneTeamOfOrganization);
    }
}