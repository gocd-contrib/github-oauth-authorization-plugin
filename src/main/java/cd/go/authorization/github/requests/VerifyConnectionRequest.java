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
import cd.go.authorization.github.executors.VerifyConnectionRequestExecutor;
import cd.go.authorization.github.models.GitHubConfiguration;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;

public class VerifyConnectionRequest extends Request {
    private final GitHubConfiguration configuration;

    private VerifyConnectionRequest(GitHubConfiguration configuration) {
        this.configuration = configuration;
    }

    public static VerifyConnectionRequest from(GoPluginApiRequest apiRequest) {
        return new VerifyConnectionRequest(GitHubConfiguration.fromJSON(apiRequest.requestBody()));
    }

    public GitHubConfiguration githubConfiguration() {
        return configuration;
    }

    @Override
    public RequestExecutor executor() {
        return new VerifyConnectionRequestExecutor(this);
    }
}
