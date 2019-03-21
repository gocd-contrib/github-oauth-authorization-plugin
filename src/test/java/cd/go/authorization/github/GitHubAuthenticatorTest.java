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
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitHubAuthenticatorTest {

    private GitHub gitHub;
    private GitHubAuthenticator authenticator;
    private MembershipChecker membershipChecker;
    private AuthConfig authConfig;
    private GitHubConfiguration gitHubConfiguration;
    private TokenInfo tokenInfo;

    @Before
    public void setUp() throws Exception {
        gitHub = mock(GitHub.class);
        authConfig = mock(AuthConfig.class);
        gitHubConfiguration = mock(GitHubConfiguration.class);
        tokenInfo = mock(TokenInfo.class);
        membershipChecker = mock(MembershipChecker.class);
        GitHubClientBuilder gitHubClientBuilder = mock(GitHubClientBuilder.class);

        when(tokenInfo.accessToken()).thenReturn("some-token");
        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);
        when(gitHubClientBuilder.fromAccessToken(tokenInfo.accessToken(), gitHubConfiguration)).thenReturn(gitHub);

        authenticator = new GitHubAuthenticator(membershipChecker, gitHubClientBuilder);
    }

    @Test
    public void shouldAuthenticateUser() throws Exception {
        final GHMyself myself = mockUser("bford", "Bob");

        when(gitHub.getMyself()).thenReturn(myself);
        when(gitHubConfiguration.organizationsAllowed()).thenReturn(Collections.emptyList());

        final LoggedInUserInfo loggedInUserInfo = authenticator.authenticate(tokenInfo, authConfig);

        assertThat(loggedInUserInfo.getUser(), is(new User("bford", "Bob", "bford@example.com")));
    }

    @Test
    public void shouldAuthenticateUserWhenUserIsAMemberOfAtLeastOneOfTheAllowedOrganization() throws Exception {
        final GHMyself myself = mockUser("bford", "Bob");
        final List<String> allowedOrganizations = asList("OrgA", "OrgB");

        when(gitHub.getMyself()).thenReturn(myself);
        when(gitHubConfiguration.organizationsAllowed()).thenReturn(allowedOrganizations);
        when(membershipChecker.isAMemberOfAtLeastOneOrganization(eq(myself), eq(authConfig), eq(allowedOrganizations))).thenReturn(true);

        final LoggedInUserInfo loggedInUserInfo = authenticator.authenticate(tokenInfo, authConfig);

        assertThat(loggedInUserInfo.getUser(), is(new User("bford", "Bob", "bford@example.com")));
    }

    @Test
    public void shouldNotAuthenticateUserWhenUserIsNotAMemberOfAnyAllowedOrganization() throws Exception {
        final GHMyself myself = mockUser("bford", "Bob");
        final List<String> allowedOrganizations = asList("OrgA", "OrgB");

        when(gitHub.getMyself()).thenReturn(myself);
        when(gitHubConfiguration.organizationsAllowed()).thenReturn(allowedOrganizations);
        when(membershipChecker.isAMemberOfAtLeastOneOrganization(eq(myself), eq(authConfig), eq(allowedOrganizations))).thenReturn(false);

        final LoggedInUserInfo loggedInUserInfo = authenticator.authenticate(tokenInfo, authConfig);

        assertNull(loggedInUserInfo);
    }

    private GHMyself mockUser(String username, String name) throws IOException {
        final GHMyself myself = mock(GHMyself.class);

        when(myself.getLogin()).thenReturn(username);
        when(myself.getEmail()).thenReturn(username + "@example.com");
        when(myself.getName()).thenReturn(name);

        return myself;
    }
}