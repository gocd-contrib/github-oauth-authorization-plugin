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
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.models.TokenInfo;
import cd.go.authorization.github.models.User;
import cd.go.authorization.github.providermanager.GitHubProviderManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GitHubAuthenticatorTest {

    private GitHubProviderManager providerManager;
    private GitHubProvider provider;
    private GitHubAuthenticator authenticator;

    @Before
    public void setUp() throws Exception {
        providerManager = mock(GitHubProviderManager.class);
        provider = mock(GitHubProvider.class);

        when(providerManager.getGitHubProvider(any(AuthConfig.class))).thenReturn(provider);

        authenticator = new GitHubAuthenticator(providerManager);
    }

    @Test
    public void authenticate_shouldAuthenticateUserUsingTokenInfoAndAuthConfig() throws Exception {
        final TokenInfo tokenInfo = mock(TokenInfo.class);
        final AuthConfig authConfig = mock(AuthConfig.class);
        final User bob = new User("bob", "B. Ford", "bob@example.com");
        final GitHubConfiguration gitHubConfiguration = mock(GitHubConfiguration.class);

        when(provider.userFromTokenInfo(tokenInfo)).thenReturn(bob);
        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);
        when(gitHubConfiguration.allowedOrganizations()).thenReturn(Collections.emptyList());

        final User user = authenticator.authenticate(tokenInfo, authConfig);

        assertThat(user, is(bob));
        verify(provider, times(0)).isAMemberOfAtLeastOneOrganization(eq(user), anyList());
    }

    @Test(expected = AuthenticationException.class)
    public void authenticate_shouldBlockUserIfUserNotBelongsToAllowedOrganizationList() throws Exception {
        final TokenInfo tokenInfo = mock(TokenInfo.class);
        final AuthConfig authConfig = mock(AuthConfig.class);
        final User bob = new User("bob", "B. Ford", "bob@example.com");
        final GitHubConfiguration gitHubConfiguration = mock(GitHubConfiguration.class);

        when(provider.userFromTokenInfo(tokenInfo)).thenReturn(bob);
        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);
        when(gitHubConfiguration.allowedOrganizations()).thenReturn(asList("gocd"));
        when(provider.isAMemberOfAtLeastOneOrganization(bob, gitHubConfiguration.allowedOrganizations())).thenReturn(false);

        authenticator.authenticate(tokenInfo, authConfig);
    }

    @Test
    public void authenticate_shouldAllowUserIfUserIsAMemberOfAtLeastOneOrganization() throws Exception {
        final TokenInfo tokenInfo = mock(TokenInfo.class);
        final AuthConfig authConfig = mock(AuthConfig.class);
        final User bob = new User("bob", "B. Ford", "bob@example.com");
        final GitHubConfiguration gitHubConfiguration = mock(GitHubConfiguration.class);

        when(provider.userFromTokenInfo(tokenInfo)).thenReturn(bob);
        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);
        when(gitHubConfiguration.allowedOrganizations()).thenReturn(asList("gocd"));
        when(provider.isAMemberOfAtLeastOneOrganization(bob, gitHubConfiguration.allowedOrganizations())).thenReturn(true);

        final User user = authenticator.authenticate(tokenInfo, authConfig);

        assertThat(user, is(bob));
    }
}