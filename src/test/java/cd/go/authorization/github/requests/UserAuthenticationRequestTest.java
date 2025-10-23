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
    public void setUp() throws Exception {
        openMocks(this);
    }

    @Test
    public void shouldDeserializeGoPluginApiRequestToUserAuthenticationRequest() throws Exception {
        String responseBody = "{\n" +
                "  \"credentials\": {\n" +
                "    \"access_token\": \"access-token\",\n" +
                "    \"token_type\": \"token\",\n" +
                "    \"scope\": \"profile\"\n" +
                "  },\n" +
                "  \"auth_configs\": [\n" +
                "    {\n" +
                "      \"id\": \"github-config\",\n" +
                "      \"configuration\": {\n" +
                "        \"PersonalAccessToken\": \"personal-access-token\",\n" +
                "        \"AuthenticateWith\": \"GitHub\",\n" +
                "        \"AllowedOrganizations\": \"org1,org2\",\n" +
                "        \"ClientId\": \"client-id\",\n" +
                "        \"ClientSecret\": \"client-secret\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"role_configs\": [\n" +
                "    {\n" +
                "      \"name\": \"admin\",\n" +
                "      \"auth_config_id\": \"github-config\",\n" +
                "      \"configuration\": {\n" +
                "        \"Organizations\": \"OrganizationFoo,OrganizationBar\",\n" +
                "        \"Teams\": \"OrganizationFoo:TeamX,TeamY\\nOrganizationBar:TeamA,TeamB\",\n" +
                "        \"Users\": \"bob,alice\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

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
