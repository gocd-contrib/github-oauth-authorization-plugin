/*
 * Copyright 2022 Thoughtworks, Inc.
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

import cd.go.authorization.github.client.AuthorizationServerArgs;
import cd.go.authorization.github.client.GitHubClientBuilder;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.AuthenticateWith;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.requests.GetAuthorizationServerUrlRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class GetAuthorizationServerUrlRequestExecutorTest {
    @Mock
    private GetAuthorizationServerUrlRequest request;
    @Mock
    private AuthConfig authConfig;
    @Mock
    private GitHubClientBuilder gitHubClientBuilder;

    private GetAuthorizationServerUrlRequestExecutor executor;

    @BeforeEach
    public void setUp() throws Exception {
        openMocks(this);

        executor = new GetAuthorizationServerUrlRequestExecutor(request, gitHubClientBuilder);
    }

    @Test
    public void shouldReturnAuthorizationServerUrlForGitHub() throws Exception {
        GitHubConfiguration gitHubConfiguration = new GitHubConfiguration("client-id", "client-secret", AuthenticateWith.GITHUB, null, "example-1");

        when(request.firstAuthConfig()).thenReturn(authConfig);
        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);
        when(request.callbackUrl()).thenReturn("call-back-url");
        when(gitHubClientBuilder.authorizationServerArgs(gitHubConfiguration, "call-back-url")).thenReturn(new AuthorizationServerArgs("foo-url", "foo-state", "foo-code-verifier"));

        final GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals("""
                {
                  "authorization_server_url": "foo-url",
                  "auth_session" : {
                    "oauth2_state": "foo-state",
                    "oauth2_code_verifier_encoded": "foo-code-verifier"
                  }
                }
                """, response.responseBody(), JSONCompareMode.NON_EXTENSIBLE);
    }
}
