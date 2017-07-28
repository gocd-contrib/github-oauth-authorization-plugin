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

package cd.go.authorization.github.executors;

import cd.go.authorization.github.GitHubAuthenticator;
import cd.go.authorization.github.GitHubAuthorizer;
import cd.go.authorization.github.GitHubClientBuilder;
import cd.go.authorization.github.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.github.models.*;
import cd.go.authorization.github.requests.UserAuthenticationRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kohsuke.github.GitHub;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserAuthenticationRequestExecutorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private UserAuthenticationRequest request;
    private AuthConfig authConfig;
    private GitHubConfiguration gitHubConfiguration;
    private GitHubAuthorizer authorizer;

    private UserAuthenticationRequestExecutor executor;
    private GitHubClientBuilder gitHubClientBuilder;
    private GitHub gitHub;
    private GitHubAuthenticator authenticator;

    @Before
    public void setUp() throws Exception {
        request = mock(UserAuthenticationRequest.class);
        authConfig = mock(AuthConfig.class);
        gitHubConfiguration = mock(GitHubConfiguration.class);
        authorizer = mock(GitHubAuthorizer.class);
        authenticator = mock(GitHubAuthenticator.class);
        gitHubClientBuilder = mock(GitHubClientBuilder.class);
        gitHub = mock(GitHub.class);

        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);
        when(gitHubClientBuilder.build("access-token", authConfig)).thenReturn(gitHub);

        executor = new UserAuthenticationRequestExecutor(request, gitHubClientBuilder, authenticator, authorizer);
    }

    @Test
    public void shouldErrorOutIfAuthConfigIsNotProvided() throws Exception {
        when(request.authConfigs()).thenReturn(Collections.emptyList());

        thrown.expect(NoAuthorizationConfigurationException.class);
        thrown.expectMessage("[Authenticate] No authorization configuration found.");

        executor.execute();
    }

    @Test
    public void shouldAuthenticateUser() throws Exception {
        final User user = new User("bford", "Bob", "bford@example.com");
        final TokenInfo tokenInfo = new TokenInfo("access-token", "token-type", "user:email,org:read");

        when(request.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(request.tokenInfo()).thenReturn(tokenInfo);
        when(authenticator.authenticate(gitHub, authConfig)).thenReturn(user);
        when(authorizer.authorize(any(User.class), eq(gitHub), anyList())).thenReturn(Collections.emptyList());

        final GoPluginApiResponse response = executor.execute();

        String expectedJSON = "{\n" +
                "  \"roles\": [],\n" +
                "  \"user\": {\n" +
                "    \"username\": \"bford\",\n" +
                "    \"display_name\": \"Bob\",\n" +
                "    \"email\": \"bford@example.com\"\n" +
                "  }\n" +
                "}";

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }


    @Test
    public void shouldAuthorizeUser() throws Exception {
        final TokenInfo tokenInfo = new TokenInfo("access-token", "token-type", "user:email,org:read");
        final User user = new User("bford", "Bob", "bford@example.com");
        final Role role = mock(Role.class);

        when(request.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(request.roles()).thenReturn(Collections.singletonList(role));
        when(request.tokenInfo()).thenReturn(tokenInfo);
        when(authenticator.authenticate(gitHub, authConfig)).thenReturn(user);
        when(authorizer.authorize(user, gitHub, request.roles())).thenReturn(Collections.singletonList("admin"));

        final GoPluginApiResponse response = executor.execute();

        String expectedJSON = "{\n" +
                "  \"roles\": [\"admin\"],\n" +
                "  \"user\": {\n" +
                "    \"username\": \"bford\",\n" +
                "    \"display_name\": \"Bob\",\n" +
                "    \"email\": \"bford@example.com\"\n" +
                "  }\n" +
                "}";

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}