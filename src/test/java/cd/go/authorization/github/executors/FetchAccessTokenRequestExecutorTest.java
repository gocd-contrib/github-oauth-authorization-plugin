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

import cd.go.authorization.github.Constants;
import cd.go.authorization.github.exceptions.AuthenticationException;
import cd.go.authorization.github.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.AuthenticateWith;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.models.TokenInfo;
import cd.go.authorization.github.requests.FetchAccessTokenRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class FetchAccessTokenRequestExecutorTest {
    private FetchAccessTokenRequest fetchAccessTokenRequest;
    private AuthConfig authConfig;
    private GitHubConfiguration gitHubConfiguration;
    private FetchAccessTokenRequestExecutor executor;
    private MockWebServer mockWebServer;

    @BeforeEach
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        fetchAccessTokenRequest = mock(FetchAccessTokenRequest.class);
        authConfig = mock(AuthConfig.class);
        gitHubConfiguration = new GitHubConfiguration("my-client", "my-secret", AuthenticateWith.GITHUB_ENTERPRISE,
                mockWebServer.url("/").toString(), "");

        when(authConfig.gitHubConfiguration()).thenReturn(gitHubConfiguration);

        executor = new FetchAccessTokenRequestExecutor(fetchAccessTokenRequest);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void shouldErrorOutIfAuthConfigIsNotProvided() {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestBody()).thenReturn("{\"auth_configs\":[]}");

        FetchAccessTokenRequestExecutor executor = new FetchAccessTokenRequestExecutor(FetchAccessTokenRequest.from(request));

        assertThrows(NoAuthorizationConfigurationException.class, executor::execute);

        verify(fetchAccessTokenRequest, never()).validateState();
    }

    @Test
    public void shouldFetchAccessToken() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new TokenInfo("token-444248275346-5758603453985735", "bearer", "user:email,read:org").toJSON()));

        when(fetchAccessTokenRequest.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(fetchAccessTokenRequest.requestParameters()).thenReturn(Collections.singletonMap("code", "code-received-in-previous-step"));


        final GoPluginApiResponse response = executor.execute();

        String expectedJSON = "{\n" +
                "  \"access_token\": \"token-444248275346-5758603453985735\",\n" +
                "  \"token_type\": \"bearer\",\n" +
                "  \"scope\": \"user:email,read:org\"\n" +
                "}";

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath(), is("/login/oauth/access_token"));
        assertThat(recordedRequest.getHeader("Content-Type"), is("application/x-www-form-urlencoded"));
        assertThat(recordedRequest.getBody().readUtf8(), is("client_id=my-client&client_secret=my-secret&code=code-received-in-previous-step"));

        verify(fetchAccessTokenRequest).validateState();
    }

    @Test
    public void fetchAccessToken_shouldErrorOutIfResponseCodeIsNot200() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        when(fetchAccessTokenRequest.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(fetchAccessTokenRequest.requestParameters()).thenReturn(Collections.singletonMap("code", "code-received-in-previous-step"));

        Exception exception = assertThrows(AuthenticationException.class, executor::execute);
        assertThat(exception.getMessage(), is("[Get Access Token] Client Error"));

        verify(fetchAccessTokenRequest).validateState();
    }

    @Test
    public void fetchAccessToken_shouldErrorIfStateDoesNotMatch() throws Exception {
        when(fetchAccessTokenRequest.authConfigs()).thenReturn(Collections.singletonList(authConfig));
        when(fetchAccessTokenRequest.authSession()).thenReturn(Map.of(Constants.AUTH_SESSION_STATE, "some-value"));
        when(fetchAccessTokenRequest.requestParameters()).thenReturn(Collections.singletonMap("code", "code-received-in-previous-step"));
        doThrow(new AuthenticationException("error validating state")).when(fetchAccessTokenRequest).validateState();

        Exception exception = assertThrows(AuthenticationException.class, executor::execute);
        assertThat(exception.getMessage(), is("error validating state"));
    }
}
