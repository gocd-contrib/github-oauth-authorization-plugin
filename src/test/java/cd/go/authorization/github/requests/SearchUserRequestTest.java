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

package cd.go.authorization.github.requests;

import cd.go.authorization.github.models.AuthConfig;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchUserRequestTest {

    @Test
    public void shouldDeserializeGoPluginApiRequestToSearchUserRequest() throws Exception {
        final GoPluginApiRequest apiRequest = mock(GoPluginApiRequest.class);
        when(apiRequest.requestBody()).thenReturn("{\n" +
                "  \"search_term\": \"bob\",\n" +
                "  \"auth_configs\": [\n" +
                "    {\n" +
                "      \"id\": \"github-config\",\n" +
                "      \"configuration\": {\n" +
                "        \"ClientId\": \"client-id\",\n" +
                "        \"ClientSecret\": \"client-secret\",\n" +
                "        \"AuthenticateWith\": \"GitHubEnterprise\",\n" +
                "        \"GitHubEnterpriseUrl\": \"http://enterprise.url\",\n" +
                "        \"PersonalAccessToken\": \"personal-access-token\",\n" +
                "        \"AllowedOrganizations\": \"org1,org2\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        final SearchUserRequest searchUserRequest = SearchUserRequest.from(apiRequest);

        assertThat(searchUserRequest.searchTerm(), is("bob"));
        assertThat(searchUserRequest.authConfigs(), hasSize(1));
        assertAuthConfig(searchUserRequest.authConfigs().get(0));

    }

    private void assertAuthConfig(AuthConfig authConfig) {
        assertThat(authConfig.getId(), CoreMatchers.is("github-config"));
        assertThat(authConfig.gitHubConfiguration().clientId(), CoreMatchers.is("client-id"));
        assertThat(authConfig.gitHubConfiguration().clientSecret(), CoreMatchers.is("client-secret"));
        assertThat(authConfig.gitHubConfiguration().personalAccessToken(), CoreMatchers.is("personal-access-token"));
        assertThat(authConfig.gitHubConfiguration().allowedOrganizations(), contains("org1", "org2"));
    }
}