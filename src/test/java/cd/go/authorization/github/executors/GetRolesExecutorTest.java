/*
 * Copyright 2019 ThoughtWorks, Inc.
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

package cd.go.authorization.github.executors;

import cd.go.authorization.github.GitHubAuthorizer;
import cd.go.authorization.github.GitHubClientBuilder;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.models.Role;
import cd.go.authorization.github.requests.GetRolesRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.mockito.InOrder;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class GetRolesExecutorTest {

    private GetRolesRequest request;
    private GetRolesExecutor executor;
    private GitHubAuthorizer authorizer;
    private GitHubClientBuilder clientBuilder;

    @Before
    public void setUp() {
        request = mock(GetRolesRequest.class);
        authorizer = mock(GitHubAuthorizer.class);
        clientBuilder = mock(GitHubClientBuilder.class);
        AuthConfig authConfig = mock(AuthConfig.class);
        when(request.getAuthConfig()).thenReturn(authConfig);
        when(request.getUsername()).thenReturn("bob");
        when(authConfig.gitHubConfiguration()).thenReturn(mock(GitHubConfiguration.class));

        executor = new GetRolesExecutor(request, authorizer, clientBuilder);
    }

    @Test
    public void shouldReturnEmptyResponseIfThereAreNoRolesProvidedFromRequest() throws Exception {
        when(clientBuilder.from(request.getAuthConfig().gitHubConfiguration())).thenReturn(mock(GitHub.class));

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), true);
        verifyZeroInteractions(authorizer);
        verifyZeroInteractions(clientBuilder);
    }

    @Test
    public void shouldReturnSuccessResponseWithRoles() throws IOException, JSONException {
        GitHub gitHub = mock(GitHub.class);
        GHUser ghUser = mock(GHUser.class);

        when(clientBuilder.from(request.getAuthConfig().gitHubConfiguration())).thenReturn(gitHub);
        when(gitHub.getUser("bob")).thenReturn(ghUser);
        when(request.getRoles()).thenReturn(rolesWithName("blackbird", "super-admin", "view"));
        when(authorizer.authorize(ghUser, request.getAuthConfig(), request.getRoles())).thenReturn(Arrays.asList("blackbird", "super-admin"));

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[\"blackbird\",\"super-admin\"]", response.responseBody(), true);

        InOrder inOrder = inOrder(clientBuilder, gitHub, authorizer);
        inOrder.verify(clientBuilder).from(request.getAuthConfig().gitHubConfiguration());
        inOrder.verify(gitHub).getUser(request.getUsername());
        inOrder.verify(authorizer).authorize(ghUser, request.getAuthConfig(), request.getRoles());
    }

    @Test
    public void shouldReturnErrorResponseWhenUserWithProvidedUsernameNotFound() throws IOException {
        GitHub gitHub = mock(GitHub.class);

        when(clientBuilder.from(request.getAuthConfig().gitHubConfiguration())).thenReturn(gitHub);
        when(gitHub.getUser("bob")).thenReturn(null);
        when(request.getRoles()).thenReturn(rolesWithName("blackbird", "super-admin", "view"));

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(500));

        InOrder inOrder = inOrder(clientBuilder, gitHub);
        inOrder.verify(clientBuilder).from(request.getAuthConfig().gitHubConfiguration());
        inOrder.verify(gitHub).getUser(request.getUsername());
        verifyZeroInteractions(authorizer);
    }

    private Role roleWithName(String name) {
        return Role.fromJSON("{\"name\":\"" + name + "\"}");
    }

    private List<Role> rolesWithName(String... names) {
        return Arrays.stream(names).map(this::roleWithName).collect(Collectors.toList());
    }
}

