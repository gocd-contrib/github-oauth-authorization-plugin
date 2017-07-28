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

import cd.go.authorization.github.exceptions.AuthenticationException;
import cd.go.authorization.github.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.models.TokenInfo;
import cd.go.authorization.github.requests.FetchAccessTokenRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FetchAccessTokenRequestExecutorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private FetchAccessTokenRequest fetchAccessTokenRequest;
    private AuthConfig authConfig;
    private GitHubConfiguration gitHubConfiguration;
    private FetchAccessTokenRequestExecutor executor;
    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        fetchAccessTokenRequest = mock(FetchAccessTokenRequest.class);
        authConfig = mock(AuthConfig.class);
        gitHubConfiguration = mock(GitHubConfiguration.class);

        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);

        executor = new FetchAccessTokenRequestExecutor(fetchAccessTokenRequest);
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test(expected = NoAuthorizationConfigurationException.class)
    public void shouldErrorOutIfAuthConfigIsNotProvided() throws Exception {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestBody()).thenReturn("{\"auth_configs\":[]}");

        FetchAccessTokenRequestExecutor executor = new FetchAccessTokenRequestExecutor(FetchAccessTokenRequest.from(request));

        executor.execute();
    }

    @Test
    public void shouldFetchAccessToken() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new TokenInfo("token-444248275346-5758603453985735", "bearer", "user:email,read:org").toJSON()));

        when(fetchAccessTokenRequest.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(fetchAccessTokenRequest.requestParameters()).thenReturn(Collections.singletonMap("code", "code-received-in-previous-step"));
        when(gitHubConfiguration.apiUrl()).thenReturn(mockWebServer.url("/").toString());

        final GoPluginApiResponse response = executor.execute();

        String expectedJSON = "{\n" +
                "  \"access_token\": \"token-444248275346-5758603453985735\",\n" +
                "  \"token_type\": \"bearer\",\n" +
                "  \"scope\": \"user:email,read:org\"\n" +
                "}";

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }

    @Test(expected = AuthenticationException.class)
    public void fetchAccessToken_shouldErrorOutIfResponseCodeIsNot200() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        when(fetchAccessTokenRequest.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(fetchAccessTokenRequest.requestParameters()).thenReturn(Collections.singletonMap("code", "code-received-in-previous-step"));
        when(gitHubConfiguration.apiUrl()).thenReturn(mockWebServer.url("/").toString());

        executor.execute();
    }
}