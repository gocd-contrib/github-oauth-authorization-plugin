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

import cd.go.authorization.github.executors.UserAuthenticationRequestExecutor;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.OAuthTokenInfo;
import cd.go.authorization.github.models.Role;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class UserAuthenticationRequestTest {
    @Mock
    private GoPluginApiRequest request;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void shouldDeserializeGoPluginApiRequestToUserAuthenticationRequest() {
        String responseBody = """
                {
                  "credentials": {
                    "access_token": "access-token",
                    "token_type": "token",
                    "scope": "profile"
                  },
                  "auth_configs": [
                    {
                      "id": "github-config",
                      "configuration": {
                        "PersonalAccessToken": "personal-access-token",
                        "AuthenticateWith": "GitHub",
                        "AllowedOrganizations": "org1,org2",
                        "ClientId": "client-id",
                        "ClientSecret": "client-secret"
                      }
                    }
                  ],
                  "role_configs": [
                    {
                      "name": "admin",
                      "auth_config_id": "github-config",
                      "configuration": {
                        "Organizations": "OrganizationFoo,OrganizationBar",
                        "Teams": "OrganizationFoo:TeamX,TeamY\\nOrganizationBar:TeamA,TeamB",
                        "Users": "bob,alice"
                      }
                    }
                  ]
                }""";

        when(request.requestBody()).thenReturn(responseBody);

        final UserAuthenticationRequest request = UserAuthenticationRequest.from(this.request);

        assertThat(request.authConfigs()).hasSize(1);
        assertThat(request.roles()).hasSize(1);
        assertThat(request.executor()).isInstanceOf(UserAuthenticationRequestExecutor.class);

        assertAuthConfig(request.firstAuthConfig());
        assertTokenInfo(request.oauthTokenInfo());
        assertRole(request.roles().get(0));
    }

    private void assertRole(Role role) {
        assertThat(role.name()).isEqualTo("admin");
        assertThat(role.authConfigId()).isEqualTo("github-config");
        assertThat(role.roleConfiguration().users()).contains("bob", "alice");
        assertThat(role.roleConfiguration().organizations()).contains("organizationfoo", "organizationbar");
        assertThat(role.roleConfiguration().teams()).containsEntry("organizationfoo", List.of("teamx", "teamy"));
        assertThat(role.roleConfiguration().teams()).containsEntry("organizationbar", List.of("teama", "teamb"));
    }

    private void assertTokenInfo(OAuthTokenInfo tokenInfo) {
        assertThat(tokenInfo.oauthAccessToken()).isEqualTo("access-token");
        assertThat(tokenInfo.tokenType()).isEqualTo("token");
        assertThat(tokenInfo.scope()).isEqualTo("profile");
    }

    private void assertAuthConfig(AuthConfig authConfig) {
        assertThat(authConfig.getId()).isEqualTo("github-config");
        assertThat(authConfig.gitHubConfiguration().clientId()).isEqualTo("client-id");
        assertThat(authConfig.gitHubConfiguration().clientSecret()).isEqualTo("client-secret");
        assertThat(authConfig.gitHubConfiguration().organizationsAllowed()).contains("org1","org2");
    }
}
