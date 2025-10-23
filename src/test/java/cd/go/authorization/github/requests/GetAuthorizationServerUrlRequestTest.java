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

import cd.go.authorization.github.executors.GetAuthorizationServerUrlRequestExecutor;
import cd.go.authorization.github.models.AuthConfig;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class GetAuthorizationServerUrlRequestTest {
    @Mock
    private GoPluginApiRequest apiRequest;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void shouldDeserializeGoPluginApiRequestToGetAuthorizationServerUrlRequest() {
        String responseBody = """
                {
                  "authorization_server_callback_url": "https://redirect.url",
                  "auth_configs": [
                    {
                      "id": "github-config",
                      "configuration": {
                        "GoServerUrl": "https://your.go.server.url",
                        "ClientId": "client-id",
                        "ClientSecret": "client-secret"
                      }
                    }
                  ]
                }""";

        when(apiRequest.requestBody()).thenReturn(responseBody);

        final GetAuthorizationServerUrlRequest request = GetAuthorizationServerUrlRequest.from(apiRequest);

        assertThat(request.authConfigs()).hasSize(1);
        assertThat(request.executor()).isInstanceOf(GetAuthorizationServerUrlRequestExecutor.class);

        final AuthConfig authConfig = request.authConfigs().get(0);

        assertThat(request.callbackUrl()).isEqualTo("https://redirect.url");

        assertThat(authConfig.getId()).isEqualTo("github-config");
        assertThat(authConfig.gitHubConfiguration().clientId()).isEqualTo("client-id");
        assertThat(authConfig.gitHubConfiguration().clientSecret()).isEqualTo("client-secret");

    }
}
