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

import cd.go.authorization.github.executors.RequestExecutor;
import cd.go.authorization.github.models.GitHubRoleConfiguration;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;

public class RoleConfigValidateRequest extends Request {
    private final GitHubRoleConfiguration gitHubRoleConfiguration;

    public RoleConfigValidateRequest(GitHubRoleConfiguration gitHubRoleConfiguration) {
        this.gitHubRoleConfiguration = gitHubRoleConfiguration;
    }

    @Override
    public RequestExecutor executor() {
        return new RoleConfigValidateRequestExecutor(this);
    }

    public GitHubRoleConfiguration gitHubRoleConfiguration() {
        return gitHubRoleConfiguration;
    }

    public static final RoleConfigValidateRequest from(GoPluginApiRequest apiRequest) {
        return new RoleConfigValidateRequest(GitHubRoleConfiguration.fromJSON(apiRequest.requestBody()));
    }

}
