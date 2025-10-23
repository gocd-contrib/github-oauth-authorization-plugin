/*
 * Copyright 2025 ThoughtWorks, Inc.
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

package cd.go.authorization.github.client;

import cd.go.authorization.github.models.AuthenticateWith;
import cd.go.authorization.github.models.GitHubConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class GitHubClientBuilderTest {

    @Mock
    private GitHubConfiguration gitHubConfiguration;
    private GitHubClientBuilder builder;

    @BeforeEach
    public void setUp() {
        openMocks(this);

        when(gitHubConfiguration.clientId()).thenReturn("client-id");
        when(gitHubConfiguration.clientSecret()).thenReturn("client-secret");
        when(gitHubConfiguration.authenticateWith()).thenReturn(AuthenticateWith.GITHUB);
        when(gitHubConfiguration.scope()).thenReturn("api");

        builder = new GitHubClientBuilder();
    }

    @Test
    public void shouldReturnAuthorizationServerArgsForGitHub() {
        final AuthorizationServerArgs authorizationServerArgs = builder.authorizationServerArgs(gitHubConfiguration, "call-back-url");

        assertThat(authorizationServerArgs).satisfies(args -> {
            assertThat(args.url()).startsWith("https://github.com/login/oauth/authorize?client_id=client-id&redirect_uri=call-back-url&response_type=code&scope=api&state=" + URLEncoder.encode(args.state(), StandardCharsets.UTF_8) + "&code_challenge_method=S256&code_challenge=");
        });
    }

    @Test
    public void shouldReturnAuthorizationServerArgsForGitHubEnterprise() {
        when(gitHubConfiguration.authenticateWith()).thenReturn(AuthenticateWith.GITHUB_ENTERPRISE);
        when(gitHubConfiguration.gitHubEnterpriseUrl()).thenReturn("http://enterprise.url");

        final AuthorizationServerArgs authorizationServerArgs = builder.authorizationServerArgs(gitHubConfiguration, "call-back-url");

        assertThat(authorizationServerArgs).satisfies(args -> {
            assertThat(args.url()).startsWith("http://enterprise.url/login/oauth/authorize?client_id=client-id&redirect_uri=call-back-url&response_type=code&scope=api&state=" + URLEncoder.encode(args.state(), StandardCharsets.UTF_8) + "&code_challenge_method=S256&code_challenge=");
        });
    }

    @Test
    public void shouldReturnAuthorizationServerArgsForGitHubEnterpriseWithTrailingSlash() {
        when(gitHubConfiguration.authenticateWith()).thenReturn(AuthenticateWith.GITHUB_ENTERPRISE);
        when(gitHubConfiguration.gitHubEnterpriseUrl()).thenReturn("http://enterprise.url/");

        final AuthorizationServerArgs authorizationServerArgs = builder.authorizationServerArgs(gitHubConfiguration, "call-back-url");

        assertThat(authorizationServerArgs).satisfies(args -> {
            assertThat(args.url()).startsWith("http://enterprise.url/login/oauth/authorize?client_id=client-id&redirect_uri=call-back-url&response_type=code&scope=api&state=" + URLEncoder.encode(args.state(), StandardCharsets.UTF_8) + "&code_challenge_method=S256&code_challenge=");
        });
    }
}