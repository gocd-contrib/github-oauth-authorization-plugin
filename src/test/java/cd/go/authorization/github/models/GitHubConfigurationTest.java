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

package cd.go.authorization.github.models;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GitHubConfigurationTest {

    @Test
    public void shouldDeserializeGitHubConfiguration() {
        final GitHubConfiguration gitHubConfiguration = GitHubConfiguration.fromJSON("""
                {
                  "ClientId": "client-id",
                  "AllowedOrganizations": "example-1,example-2",
                  "AuthenticateWith": "GitHubEnterprise",
                  "GitHubEnterpriseUrl": "https://enterprise.url",
                  "ClientSecret": "client-secret",
                  "PersonalAccessToken": "personal-access-token",
                  "AuthorizeUsing": "PersonalAccessToken"
                }""");

        assertThat(gitHubConfiguration.clientId()).isEqualTo("client-id");
        assertThat(gitHubConfiguration.clientSecret()).isEqualTo("client-secret");
        assertThat(gitHubConfiguration.organizationsAllowed()).contains("example-1", "example-2");
        assertThat(gitHubConfiguration.gitHubEnterpriseUrl()).isEqualTo("https://enterprise.url");
        assertThat(gitHubConfiguration.authenticateWith()).isEqualTo(AuthenticateWith.GITHUB_ENTERPRISE);
        assertThat(gitHubConfiguration.personalAccessToken()).isEqualTo("personal-access-token");
    }

    @Test
    public void shouldSerializeToJSON() throws Exception {
        GitHubConfiguration gitHubConfiguration = new GitHubConfiguration("client-id", "client-secret",
                AuthenticateWith.GITHUB_ENTERPRISE, "http://enterprise.url", "example-1");

        String expectedJSON = """
                {
                  "ClientId": "client-id",
                  "ClientSecret": "client-secret",
                  "AuthenticateWith": "GitHubEnterprise",
                  "GitHubEnterpriseUrl": "http://enterprise.url",
                  "AllowedOrganizations": "example-1"
                }""";

        JSONAssert.assertEquals(expectedJSON, gitHubConfiguration.toJSON(), true);

    }

    @Test
    public void shouldConvertConfigurationToProperties() {
        GitHubConfiguration gitHubConfiguration = new GitHubConfiguration("client-id", "client-secret", AuthenticateWith.GITHUB_ENTERPRISE, "http://enterprise.url", "example-1");

        final Map<String, String> properties = gitHubConfiguration.toProperties();

        assertThat(properties).containsEntry("ClientId", "client-id");
        assertThat(properties).containsEntry("ClientSecret", "client-secret");
        assertThat(properties).containsEntry("AllowedOrganizations", "example-1");
        assertThat(properties).containsEntry("AuthenticateWith", "GitHubEnterprise");
        assertThat(properties).containsEntry("GitHubEnterpriseUrl", "http://enterprise.url");
    }
}
