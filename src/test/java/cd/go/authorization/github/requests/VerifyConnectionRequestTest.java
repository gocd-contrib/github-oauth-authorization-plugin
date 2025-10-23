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

import cd.go.authorization.github.executors.VerifyConnectionRequestExecutor;
import cd.go.authorization.github.models.AuthenticateWith;
import cd.go.authorization.github.models.GitHubConfiguration;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class VerifyConnectionRequestTest {
    @Mock
    private GoPluginApiRequest apiRequest;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void shouldDeserializeGoPluginApiRequestToVerifyConnectionRequest() {
        String responseBody = """
                {
                  "ClientId": "client-id",
                  "ClientSecret": "client-secret",
                  "AuthenticateWith": "GitHubEnterprise",
                  "GitHubEnterpriseUrl": "my-enterprise-url",
                  "AllowedOrganizations": "Foo,Bar"
                }""";

        when(apiRequest.requestBody()).thenReturn(responseBody);

        final VerifyConnectionRequest request = VerifyConnectionRequest.from(apiRequest);
        final GitHubConfiguration gitHubConfiguration = request.githubConfiguration();

        assertThat(request.executor()).isInstanceOf(VerifyConnectionRequestExecutor.class);

        assertThat(gitHubConfiguration.clientId()).isEqualTo("client-id");
        assertThat(gitHubConfiguration.clientSecret()).isEqualTo("client-secret");
        assertThat(gitHubConfiguration.authenticateWith()).isEqualTo(AuthenticateWith.GITHUB_ENTERPRISE);
        assertThat(gitHubConfiguration.gitHubEnterpriseUrl()).isEqualTo("my-enterprise-url");
        assertThat(gitHubConfiguration.organizationsAllowed()).contains("foo", "bar");
    }

}
