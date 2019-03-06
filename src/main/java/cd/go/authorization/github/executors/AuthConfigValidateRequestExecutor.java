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

package cd.go.authorization.github.executors;


import cd.go.authorization.github.annotation.MetadataValidator;
import cd.go.authorization.github.annotation.ValidationResult;
import cd.go.authorization.github.models.AuthenticateWith;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.requests.AuthConfigValidateRequest;
import cd.go.authorization.github.utils.Util;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;


public class AuthConfigValidateRequestExecutor implements RequestExecutor {
    private final AuthConfigValidateRequest request;

    public AuthConfigValidateRequestExecutor(AuthConfigValidateRequest request) {
        this.request = request;
    }

    public GoPluginApiResponse execute() {
        final GitHubConfiguration gitHubConfiguration = request.githubConfiguration();
        final ValidationResult validationResult = new MetadataValidator().validate(gitHubConfiguration);

        if (gitHubConfiguration.authenticateWith() == AuthenticateWith.GITHUB_ENTERPRISE && Util.isBlank(gitHubConfiguration.gitHubEnterpriseUrl())) {
            validationResult.addError("GitHubEnterpriseUrl", "GitHubEnterpriseUrl must not be blank.");
        }

        if (Util.isBlank(gitHubConfiguration.personalAccessToken())) {
            validationResult.addError("PersonalAccessToken", "PersonalAccessToken must not be blank.");
        }

        return DefaultGoPluginApiResponse.success(validationResult.toJSON());
    }
}
