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

package cd.go.authorization.github.requests;

import cd.go.authorization.github.exceptions.AuthenticationException;
import cd.go.authorization.github.executors.FetchAccessTokenRequestExecutor;
import cd.go.authorization.github.models.AuthConfig;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class FetchAccessTokenRequestTest {
    public static final String WITH_NO_SESSION_RESPONSE = "{\n" +
            "  \"auth_configs\": [\n" +
            "    {\n" +
            "      \"id\": \"github-auth-config\",\n" +
            "      \"configuration\": {\n" +
            "        \"GoServerUrl\": \"https://your.go.server.url\",\n" +
            "        \"ClientId\": \"client-id\",\n" +
            "        \"ClientSecret\": \"client-secret\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public static final String WITH_SESSION_STATE_RESPONSE = "{\n" +
            "  \"auth_configs\": [\n" +
            "    {\n" +
            "      \"id\": \"github-auth-config\",\n" +
            "      \"configuration\": {\n" +
            "        \"GoServerUrl\": \"https://your.go.server.url\",\n" +
            "        \"ClientId\": \"client-id\",\n" +
            "        \"ClientSecret\": \"client-secret\"\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"auth_session\": {" +
            "    \"oauth2_state\": \"some-state-value\"" +
            "  }\n" +
            "}";
    public static final String WITH_EMPTY_SESSION_RESPONSE = "{\n" +
            "  \"auth_configs\": [\n" +
            "    {\n" +
            "      \"id\": \"github-auth-config\",\n" +
            "      \"configuration\": {\n" +
            "        \"GoServerUrl\": \"https://your.go.server.url\",\n" +
            "        \"ClientId\": \"client-id\",\n" +
            "        \"ClientSecret\": \"client-secret\"\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"auth_session\": {}\n" +
            "}";
    @Mock
    private GoPluginApiRequest apiRequest;

    @BeforeEach
    public void setUp() throws Exception {
        openMocks(this);
    }

    @Test
    public void shouldDeserializeGoPluginApiRequestToFetchAccessTokenRequest() throws Exception {

        when(apiRequest.requestBody()).thenReturn(WITH_NO_SESSION_RESPONSE);

        final FetchAccessTokenRequest request = FetchAccessTokenRequest.from(apiRequest);

        assertThat(request.authConfigs(), hasSize(1));
        assertThat(request.authSession(), nullValue());
        assertThat(request.executor(), instanceOf(FetchAccessTokenRequestExecutor.class));

        final AuthConfig authConfig = request.authConfigs().get(0);
        assertThat(authConfig.getId(), is("github-auth-config"));
        assertThat(authConfig.gitHubConfiguration().clientId(), is("client-id"));
        assertThat(authConfig.gitHubConfiguration().clientSecret(), is("client-secret"));
    }

    @Test
    public void validateStateShouldSucceedWhenNoSessionIsPresent() {
        final FetchAccessTokenRequest request = new FetchAccessTokenRequest();
        request.validateState();
    }

    @Test
    public void validateStateShouldSucceedWhenSessionStateMatchesRequest() {
        when(apiRequest.requestBody()).thenReturn(WITH_SESSION_STATE_RESPONSE);
        when(apiRequest.requestParameters()).thenReturn(Map.of("state", "some-state-value"));

        final FetchAccessTokenRequest request = FetchAccessTokenRequest.from(apiRequest);
        request.validateState();
    }

    @Test
    public void validateStateShouldFailWhenRequestParamIsMissing() {
        when(apiRequest.requestBody()).thenReturn(WITH_SESSION_STATE_RESPONSE);

        final FetchAccessTokenRequest request = FetchAccessTokenRequest.from(apiRequest);
        Exception error = assertThrows(NullPointerException.class, request::validateState);
        assertThat(error.getMessage(), is("OAuth2 state is missing from redirect"));
    }

    @Test
    public void validateStateShouldFailWhenSessionStateIsMissing() {
        when(apiRequest.requestBody()).thenReturn(WITH_EMPTY_SESSION_RESPONSE);
        when(apiRequest.requestParameters()).thenReturn(Map.of("state", "some-state-value"));

        final FetchAccessTokenRequest request = FetchAccessTokenRequest.from(apiRequest);
        Exception error = assertThrows(NullPointerException.class, request::validateState);
        assertThat(error.getMessage(), is("OAuth2 state is missing from session"));
    }

    @Test
    public void validateStateShouldFailWhenSessionStateDoesntMatch() {
        when(apiRequest.requestBody()).thenReturn(WITH_SESSION_STATE_RESPONSE);
        when(apiRequest.requestParameters()).thenReturn(Map.of("state", "some-other-state-value"));

        final FetchAccessTokenRequest request = FetchAccessTokenRequest.from(apiRequest);
        Exception error = assertThrows(AuthenticationException.class, request::validateState);
        assertThat(error.getMessage(), is("Redirected OAuth2 state from GitHub did not match previously generated state stored in session"));
    }
}
