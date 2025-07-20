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

import cd.go.authorization.github.executors.UserAuthenticationRequestExecutor;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.Role;
import cd.go.authorization.github.models.TokenInfo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;

import java.util.List;

public class UserAuthenticationRequest extends Request {
    @Expose
    @SerializedName("auth_configs")
    private List<AuthConfig> authConfigs;

    @Expose
    @SerializedName("credentials")
    private TokenInfo tokenInfo;

    @Expose
    @SerializedName("role_configs")
    private List<Role> roles;

    public static UserAuthenticationRequest from(GoPluginApiRequest apiRequest) {
        return Request.from(apiRequest, UserAuthenticationRequest.class);
    }

    public List<AuthConfig> authConfigs() {
        return authConfigs;
    }

    public List<Role> roles() {
        return roles;
    }

    public TokenInfo tokenInfo() {
        return tokenInfo;
    }

    @Override
    public UserAuthenticationRequestExecutor executor() {
        return new UserAuthenticationRequestExecutor(this);
    }
}
