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

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class GitHubRoleConfigurationTest {

    @Test
    public void shouldDeserializeFromJson() throws Exception {
        final String json = "{\n" +
                "  \"Organizations\": \"OrganizationFoo,OrganizationBar\",\n" +
                "  \"Teams\": \"OrganizationFoo:TeamX,TeamY\\nOrganizationBar:TeamA,TeamB\",\n" +
                "  \"Users\": \"bob,alice\"\n" +
                "}";

        final GitHubRoleConfiguration gitHubRoleConfiguration = GitHubRoleConfiguration.fromJSON(json);

        assertThat(gitHubRoleConfiguration.users(), contains("bob", "alice"));
        assertThat(gitHubRoleConfiguration.organizations(), contains("organizationfoo", "organizationbar"));
        assertThat(gitHubRoleConfiguration.teams(), hasEntry("organizationfoo", asList("teamx", "teamy")));
        assertThat(gitHubRoleConfiguration.teams(), hasEntry("organizationbar", asList("teama", "teamb")));
    }
}