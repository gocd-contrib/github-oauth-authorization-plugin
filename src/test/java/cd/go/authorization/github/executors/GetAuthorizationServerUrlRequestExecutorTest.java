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

import cd.go.authorization.github.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.AuthenticateWith;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.requests.GetAuthorizationServerUrlRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class GetAuthorizationServerUrlRequestExecutorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private GetAuthorizationServerUrlRequest request;
    @Mock
    private AuthConfig authConfig;

    private GetAuthorizationServerUrlRequestExecutor executor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        executor = new GetAuthorizationServerUrlRequestExecutor(request);
    }

    @Test
    public void shouldErrorOutIfAuthConfigIsNotProvided() throws Exception {
        when(request.authConfigs()).thenReturn(Collections.emptyList());

        thrown.expect(NoAuthorizationConfigurationException.class);
        thrown.expectMessage("[Authorization Server Url] No authorization configuration found.");

        executor.execute();
    }

    @Test
    public void shouldReturnAuthorizationServerUrlForGitHub() throws Exception {
        GitHubConfiguration gitHubConfiguration = new GitHubConfiguration("client-id", "client-secret", AuthenticateWith.GITHUB, null, "example-1");

        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);
        when(request.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(request.callbackUrl()).thenReturn("call-back-url");

        final GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), startsWith("{\"authorization_server_url\":\"https://github.com/login/oauth/authorize?client_id\\u003dclient-id\\u0026redirect_uri\\u003dcall-back-url\\u0026scope\\u003duser%3Aemail\"}"));
    }

    @Test
    public void shouldReturnAuthorizationServerUrlWithTrailingSlash() throws Exception {
        GitHubConfiguration gitHubConfiguration = new GitHubConfiguration("client-id", "client-secret", AuthenticateWith.GITHUB_ENTERPRISE, "http://enterprise.url/", "example-1");

        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);
        when(request.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(request.callbackUrl()).thenReturn("call-back-url");

        final GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), startsWith("{\"authorization_server_url\":\"http://enterprise.url/login/oauth/authorize?client_id\\u003dclient-id\\u0026redirect_uri\\u003dcall-back-url\\u0026scope\\u003duser%3Aemail\"}"));
    }

    @Test
    public void shouldReturnAuthorizationServerUrlForGitHubEnterprise() throws Exception {
        GitHubConfiguration gitHubConfiguration = new GitHubConfiguration("client-id", "client-secret", AuthenticateWith.GITHUB_ENTERPRISE, "http://enterprise.url", "example-1");

        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);
        when(request.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(request.callbackUrl()).thenReturn("call-back-url");

        final GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        assertThat(response.responseBody(), startsWith("{\"authorization_server_url\":\"http://enterprise.url/login/oauth/authorize?client_id\\u003dclient-id\\u0026redirect_uri\\u003dcall-back-url\\u0026scope\\u003duser%3Aemail\"}"));
    }
}