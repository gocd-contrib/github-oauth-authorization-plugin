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

import cd.go.authorization.github.models.GitHubConfiguration;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class AuthConfigValidateRequestTest {
    @Mock
    private GoPluginApiRequest apiRequest;

    @BeforeEach
    public void setUp() throws Exception {
        openMocks(this);
    }

    @Test
    public void shouldDeserializeGoPluginApiRequestToAuthConfigValidateRequest() throws Exception {
        String responseBody = "{\n" +
                "  \"GoServerUrl\": \"https://your.go.server.url\",\n" +
                "  \"ClientId\": \"client-id\",\n" +
                "  \"ClientSecret\": \"client-secret\"\n" +
                "}";

        when(apiRequest.requestBody()).thenReturn(responseBody);

        final AuthConfigValidateRequest request = AuthConfigValidateRequest.from(apiRequest);
        final GitHubConfiguration gitHubConfiguration = request.githubConfiguration();

        assertThat(gitHubConfiguration.clientId()).isEqualTo("client-id");
        assertThat(gitHubConfiguration.clientSecret()).isEqualTo("client-secret");
    }
}
