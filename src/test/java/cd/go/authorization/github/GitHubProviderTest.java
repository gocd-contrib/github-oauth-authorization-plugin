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

import cd.go.authorization.github.exceptions.AuthenticationException;
import cd.go.authorization.github.models.AuthenticateWith;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.models.TokenInfo;
import cd.go.authorization.github.models.User;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.AccessGrant;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.util.Arrays;

import static java.util.Collections.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GitHubProviderTest {

    private GitHubProvider provider;
    private GitHub gitHub;
    private SocialAuthManager socialAuthManager;
    private GitHubConfiguration gitHubConfiguration;

    @Before
    public void setUp() throws Exception {
        gitHubConfiguration = mock(GitHubConfiguration.class);
        socialAuthManager = mock(SocialAuthManager.class);
        gitHub = mock(GitHub.class);

        provider = new GitHubProvider(gitHubConfiguration, socialAuthManager, gitHub);
    }

    @Test
    public void authorizationServerUrl_shouldGetAuthorizationServerUrl() throws Exception {
        when(socialAuthManager.getAuthenticationUrl(provider.providerName(), "callback-url")).thenReturn("authorization-server-url");

        final String authorizationServerUrl = provider.authorizationServerUrl("callback-url");

        assertThat(authorizationServerUrl, is("authorization-server-url"));
        verifyZeroInteractions(gitHub);
    }

    @Test
    public void accessToken_shouldGetAccessTokenUsingOneTimeAccessCode() throws Exception {
        final AccessGrant accessGrant = mock(AccessGrant.class);

        when(gitHubConfiguration.authenticateWith()).thenReturn(AuthenticateWith.GITHUB);
        when(accessGrant.getProviderId()).thenReturn(provider.providerName());
        when(accessGrant.getKey()).thenReturn("access-token");
        when(accessGrant.getPermission()).thenReturn(new Permission(AuthenticateWith.GITHUB.permission()));
        when(socialAuthManager.createAccessGrant(provider.providerName(), "some-code", "")).thenReturn(accessGrant);

        final TokenInfo tokenInfo = provider.accessToken(singletonMap("code", "some-code"));

        assertNotNull(tokenInfo);
        assertThat(tokenInfo.scope(), is(provider.permission().getScope()));
        assertThat(tokenInfo.accessToken(), is("access-token"));
        verifyZeroInteractions(gitHub);
    }

    @Test
    public void userFromTokenInfo_shouldGetUserUsingAccessToken() throws Exception {
        final TokenInfo tokenInfo = mock(TokenInfo.class);
        final AccessGrant accessGrant = mock(AccessGrant.class);
        final AuthProvider authProvider = mock(AuthProvider.class);
        final Profile profile = mock(Profile.class);

        when(tokenInfo.toAccessGrant()).thenReturn(accessGrant);
        when(socialAuthManager.connect(accessGrant)).thenReturn(authProvider);
        when(authProvider.getUserProfile()).thenReturn(profile);
        when(profile.getDisplayName()).thenReturn("bob");
        when(profile.getEmail()).thenReturn("bob@example.com");
        when(profile.getFullName()).thenReturn("B. Ford");

        final User user = provider.userFromTokenInfo(tokenInfo);

        assertThat(user, is(new User("bob", "B. Ford", "bob@example.com")));
        verifyZeroInteractions(gitHub);
    }

    @Test(expected = AuthenticationException.class)
    public void userFromTokenInfo_shouldErrorOutIfAccessTokenIsNotValid() throws Exception {
        final TokenInfo tokenInfo = mock(TokenInfo.class);
        final AccessGrant accessGrant = mock(AccessGrant.class);

        when(tokenInfo.toAccessGrant()).thenReturn(accessGrant);
        when(socialAuthManager.connect(accessGrant)).thenThrow(new RuntimeException("Invalid access grant"));

        final User user = provider.userFromTokenInfo(tokenInfo);

        assertThat(user, is(new User("bob", "B. Ford", "bob@example.com")));
        verifyZeroInteractions(gitHub);
    }

    @Test
    public void isAMemberOfAtLeastOneOrganization_shouldReturnTrueIfUserIsAMemberOfAtLeastOneOrganization() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final GHUser ghUser = mock(GHUser.class);
        final GHOrganization gocdContrib = mock(GHOrganization.class);

        when(gitHub.getUser("bob")).thenReturn(ghUser);
        when(gitHub.getOrganization("gocd")).thenReturn(null);
        when(gitHub.getOrganization("gocd-contrib")).thenReturn(gocdContrib);
        when(ghUser.isMemberOf(gocdContrib)).thenReturn(true);

        final boolean aMemberOfAtLeastOneOrganization = provider.isAMemberOfAtLeastOneOrganization(user, Arrays.asList("gocd", "gocd-contrib"));

        assertTrue(aMemberOfAtLeastOneOrganization);
        verifyZeroInteractions(socialAuthManager);
    }

    @Test
    public void isAMemberOfAtLeastOneOrganization_shouldReturnFalseIfUserIsNotAMemberOfAnyOrganization() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final GHUser ghUser = mock(GHUser.class);
        final GHOrganization gocdContrib = mock(GHOrganization.class);

        when(gitHub.getUser("bob")).thenReturn(ghUser);
        when(gitHub.getOrganization("gocd")).thenReturn(null);
        when(gitHub.getOrganization("gocd-contrib")).thenReturn(gocdContrib);
        when(ghUser.isMemberOf(gocdContrib)).thenReturn(false);

        final boolean aMemberOfAtLeastOneOrganization = provider.isAMemberOfAtLeastOneOrganization(user, Arrays.asList("gocd", "gocd-contrib"));

        assertFalse(aMemberOfAtLeastOneOrganization);
        verifyZeroInteractions(socialAuthManager);
    }

    @Test
    public void isAMemberOfAtLeastOneTeamOfOrganization_shouldReturnTrueIfUserIsAMemberOfAtLeastOneTeamOfOrganization() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final GHUser ghUser = mock(GHUser.class);
        final GHTeam ghTeam = mock(GHTeam.class);

        when(gitHub.getUser("bob")).thenReturn(ghUser);
        when(gitHub.getMyTeams()).thenReturn(singletonMap("gocd", singleton(ghTeam)));
        when(ghUser.isMemberOf(ghTeam)).thenReturn(true);

        final boolean aMemberOfAtLeastOneOrganization = provider.isAMemberOfAtLeastOneTeamOfOrganization(user, singletonMap("gocd", singletonList("A-Team")));

        assertTrue(aMemberOfAtLeastOneOrganization);
        verifyZeroInteractions(socialAuthManager);
    }

    @Test
    public void isAMemberOfAtLeastOneTeamOfOrganization_shouldReturnFalseIfUserIsNotAMemberOfAnyTeamOfOrganization() throws Exception {
        final User user = new User("bob", "B. Ford", "bob@example.com");
        final GHUser ghUser = mock(GHUser.class);
        final GHTeam ghTeam = mock(GHTeam.class);

        when(gitHub.getUser("bob")).thenReturn(ghUser);
        when(gitHub.getMyTeams()).thenReturn(singletonMap("gocd", singleton(ghTeam)));
        when(ghUser.isMemberOf(ghTeam)).thenReturn(false);

        final boolean aMemberOfAtLeastOneOrganization = provider.isAMemberOfAtLeastOneTeamOfOrganization(user, singletonMap("gocd", singletonList("A-Team")));

        assertFalse(aMemberOfAtLeastOneOrganization);
        verifyZeroInteractions(socialAuthManager);
    }
}
