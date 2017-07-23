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

import cd.go.authorization.github.GitHubProvider;
import cd.go.authorization.github.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.github.providermanager.GitHubProviderManager;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.models.TokenInfo;
import cd.go.authorization.github.requests.FetchAccessTokenRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FetchAccessTokenRequestExecutorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private FetchAccessTokenRequest request;
    @Mock
    private AuthConfig authConfig;
    @Mock
    private GitHubConfiguration pluginConfiguration;
    @Mock
    private GitHubProvider provider;
    @Mock
    private GitHubProviderManager providerManager;

    private FetchAccessTokenRequestExecutor executor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(authConfig.gitHubConfiguration()).thenReturn(pluginConfiguration);
        when(providerManager.getGitHubProvider(authConfig)).thenReturn(provider);

        executor = new FetchAccessTokenRequestExecutor(request, providerManager);
    }

    @Test(expected = NoAuthorizationConfigurationException.class)
    public void shouldErrorOutIfAuthConfigIsNotProvided() throws Exception {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestBody()).thenReturn("{\"auth_configs\":[]}");

        FetchAccessTokenRequestExecutor executor = new FetchAccessTokenRequestExecutor(FetchAccessTokenRequest.from(request), providerManager);

        executor.execute();
    }

    @Test
    public void shouldFetchAccessToken() throws Exception {
        final StubbedTokenInfo tokenInfo = new StubbedTokenInfo("github", "access-token", "secret", "token", "profile", "id-token");

        when(request.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(request.requestParameters()).thenReturn(Collections.singletonMap("code", "code-received-in-previous-step"));
        when(provider.accessToken(request.requestParameters())).thenReturn(tokenInfo);

        final GoPluginApiResponse response = executor.execute();


        String expectedJSON = "{\n" +
                "  \"provider_id\": \"github\",\n" +
                "  \"access_token\": \"access-token\",\n" +
                "  \"secret\": \"secret\",\n" +
                "  \"token_type\": \"token\",\n" +
                "  \"scope\": \"profile\",\n" +
                "  \"id_token\": \"id-token\"\n" +
                "}";

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }

    private class StubbedTokenInfo extends TokenInfo {
        public StubbedTokenInfo(String providerId, String accessToken, String secret, String tokenType, String scope, String idToken) {
            super(providerId, accessToken, secret, tokenType, scope, idToken);
        }
    }
}