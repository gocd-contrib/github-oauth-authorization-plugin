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
import cd.go.authorization.github.requests.GetRolesRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.List;

import static cd.go.authorization.github.GitHubPlugin.LOG;
import static cd.go.authorization.github.utils.Util.GSON;
import static java.lang.String.format;

public class GetRolesExecutor implements RequestExecutor {
    private final GetRolesRequest request;
    private GitHubClientBuilder clientBuilder;
    private final GitHubAuthorizer gitHubAuthorizer;

    public GetRolesExecutor(GetRolesRequest request) {
        this(request, new GitHubAuthorizer(), new GitHubClientBuilder());
    }

    GetRolesExecutor(GetRolesRequest request, GitHubAuthorizer gitHubAuthorizer, GitHubClientBuilder clientBuilder) {
        this.request = request;
        this.clientBuilder = clientBuilder;
        this.gitHubAuthorizer = gitHubAuthorizer;
    }

    @Override
    public GoPluginApiResponse execute() throws IOException {
        if (request.getRoles().isEmpty()) {
            LOG.debug("[Get User Roles] Server sent empty roles config. Nothing to do!.");
            return DefaultGoPluginApiResponse.success("[]");
        }

        GitHub gitHub = clientBuilder.from(request.getAuthConfig().gitHubConfiguration());
        GHUser user = gitHub.getUser(request.getUsername());

        if (user == null) {
            LOG.error(format("[Get User Roles] User %s does not exist in GitHub.", request.getUsername()));
            return DefaultGoPluginApiResponse.error("");
        }

        List<String> roles = gitHubAuthorizer.authorize(user, request.getAuthConfig(), request.getRoles());

        LOG.debug(format("[Get User Roles] User %s has %s roles.", request.getUsername(), roles));
        return DefaultGoPluginApiResponse.success(GSON.toJson(roles));
    }
}

