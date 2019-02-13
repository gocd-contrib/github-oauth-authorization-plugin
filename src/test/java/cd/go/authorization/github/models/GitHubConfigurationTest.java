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

package cd.go.authorization.github.models;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class GitHubConfigurationTest {

    @Test
    public void shouldDeserializeGitHubConfiguration() {
        final GitHubConfiguration gitHubConfiguration = GitHubConfiguration.fromJSON("{\n" +
                "  \"ClientId\": \"client-id\",\n" +
                "  \"AllowedOrganizations\": \"example-1,example-2\",\n" +
                "  \"AuthenticateWith\": \"GitHubEnterprise\",\n" +
                "  \"GitHubEnterpriseUrl\": \"https://enterprise.url\",\n" +
                "  \"ClientSecret\": \"client-secret\",\n" +
                "  \"PersonalAccessToken\": \"personal-access-token\",\n" +
                "  \"AuthorizeUsing\": \"PersonalAccessToken\"\n" +
                "}");

        assertThat(gitHubConfiguration.clientId(), is("client-id"));
        assertThat(gitHubConfiguration.clientSecret(), is("client-secret"));
        assertThat(gitHubConfiguration.organizationsAllowed(), contains("example-1", "example-2"));
        assertThat(gitHubConfiguration.gitHubEnterpriseUrl(), is("https://enterprise.url"));
        assertThat(gitHubConfiguration.authenticateWith(), is(AuthenticateWith.GITHUB_ENTERPRISE));
        assertThat(gitHubConfiguration.personalAccessToken(), is("personal-access-token"));
    }

    @Test
    public void shouldSerializeToJSON() throws Exception {
        GitHubConfiguration gitHubConfiguration = new GitHubConfiguration("client-id", "client-secret",
                AuthenticateWith.GITHUB_ENTERPRISE, "http://enterprise.url", "example-1");

        String expectedJSON = "{\n" +
                "  \"ClientId\": \"client-id\",\n" +
                "  \"ClientSecret\": \"client-secret\",\n" +
                "  \"AuthenticateWith\": \"GitHubEnterprise\",\n" +
                "  \"GitHubEnterpriseUrl\": \"http://enterprise.url\",\n" +
                "  \"AllowedOrganizations\": \"example-1\"\n" +
                "}";

        JSONAssert.assertEquals(expectedJSON, gitHubConfiguration.toJSON(), true);

    }

    @Test
    public void shouldConvertConfigurationToProperties() {
        GitHubConfiguration gitHubConfiguration = new GitHubConfiguration("client-id", "client-secret", AuthenticateWith.GITHUB_ENTERPRISE, "http://enterprise.url", "example-1");

        final Map<String, String> properties = gitHubConfiguration.toProperties();

        assertThat(properties, hasEntry("ClientId", "client-id"));
        assertThat(properties, hasEntry("ClientSecret", "client-secret"));
        assertThat(properties, hasEntry("AllowedOrganizations", "example-1"));
        assertThat(properties, hasEntry("AuthenticateWith", "GitHubEnterprise"));
        assertThat(properties, hasEntry("GitHubEnterpriseUrl", "http://enterprise.url"));
    }
}