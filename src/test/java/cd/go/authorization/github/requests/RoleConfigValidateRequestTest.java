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

import cd.go.authorization.github.models.GitHubRoleConfiguration;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class RoleConfigValidateRequestTest {

    @Mock
    private GoPluginApiRequest apiRequest;

    @BeforeEach
    public void setUp() throws Exception {
        openMocks(this);
    }

    @Test
    public void shouldDeserializeGoPluginApiRequestToRoleConfigValidateRequest() throws Exception {
        String responseBody = "{\n" +
                "  \"Organizations\": \"Org1,Org2,Org3\",\n" +
                "  \"Teams\": \"Org4:team-1,team-2\",\n" +
                "  \"Users\": \"bob,alice\"\n" +
                "}";

        when(apiRequest.requestBody()).thenReturn(responseBody);

        final RoleConfigValidateRequest request = RoleConfigValidateRequest.from(apiRequest);
        final GitHubRoleConfiguration gitHubRoleConfiguration = request.gitHubRoleConfiguration();

        assertThat(gitHubRoleConfiguration.organizations()).contains("org1", "org2", "org3");
        assertThat(gitHubRoleConfiguration.teams()).containsEntry("org4", List.of("team-1", "team-2"));
        assertThat(gitHubRoleConfiguration.users()).contains("bob", "alice");
    }
}
