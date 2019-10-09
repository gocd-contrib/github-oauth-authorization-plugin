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

import cd.go.authorization.github.GitHubClientBuilder;
import cd.go.authorization.github.requests.ValidateUserRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import static cd.go.authorization.github.GitHubPlugin.LOG;
import static java.lang.String.format;

public class ValidateUserRequestExecutor implements RequestExecutor {
    private ValidateUserRequest request;
    private final GitHubClientBuilder clientBuilder;

    public ValidateUserRequestExecutor(ValidateUserRequest request) {
        this(request, new GitHubClientBuilder());
    }

    ValidateUserRequestExecutor(ValidateUserRequest request, GitHubClientBuilder clientBuilder) {
        this.request = request;
        this.clientBuilder = clientBuilder;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        GitHub gitHub = clientBuilder.from(request.getAuthConfig().gitHubConfiguration());
        GHUser user = gitHub.getUser(request.getUsername());
        if (user == null) {
            LOG.error(format("[Is Valid User] User %s does not exist in GitHub.", request.getUsername()));
            return DefaultGoPluginApiResponse.error(String.format("User '%s' does not exist in GitHub.", request.getUsername()));
        } else {
            LOG.debug(format("[Is Valid User] %s is valid user.", request.getUsername()));
            return DefaultGoPluginApiResponse.success("");
        }
    }
}
