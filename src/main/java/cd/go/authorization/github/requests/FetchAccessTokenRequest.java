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

import cd.go.authorization.github.Constants;
import cd.go.authorization.github.exceptions.AuthenticationException;
import cd.go.authorization.github.executors.FetchAccessTokenRequestExecutor;
import cd.go.authorization.github.executors.RequestExecutor;
import cd.go.authorization.github.models.AuthConfig;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FetchAccessTokenRequest extends Request implements AuthConfigurable {
    private static final Logger LOGGER = Logger.getLoggerFor(FetchAccessTokenRequest.class);

    @Expose
    @SerializedName("auth_configs")
    private List<AuthConfig> authConfigs;

    @Expose
    @SerializedName("auth_session")
    private Map<String, String> authSession;

    public static FetchAccessTokenRequest from(GoPluginApiRequest apiRequest) {
        return Request.from(apiRequest, FetchAccessTokenRequest.class);
    }

    @Override
    public List<AuthConfig> authConfigs() {
        return authConfigs;
    }

    public Map<String, String> authSession() {
        return authSession;
    }

    @Override
    public RequestExecutor executor() {
        return new FetchAccessTokenRequestExecutor(this);
    }

    public void validateState() {
        // GoCD versions prior to 23.2.0 don't return state, so we can't validate it, this is for backward compatibility
        if (authSession == null) {
            LOGGER.info("Skipped OAuth2 `state` validation, GoCD server < 23.2.0 does not support propagating auth_session parameters");
            return;
        }

        String redirectedState = Objects.requireNonNull(requestParameters().get("state"), "OAuth2 state is missing from redirect");
        String sessionState = Objects.requireNonNull(authSession.get(Constants.AUTH_SESSION_STATE), "OAuth2 state is missing from session");
        if (!MessageDigest.isEqual(redirectedState.getBytes(), sessionState.getBytes())) {
            throw new AuthenticationException("Redirected OAuth2 state from GitHub did not match previously generated state stored in session");
        }
    }

    public String authorizationCode() {
        return Objects.requireNonNullElseGet(requestParameters().get("code"), () -> { throw new IllegalArgumentException("[Fetch Access Token] Expecting `code` in request params, but not received."); });
    }

    public String codeVerifierEncoded() {
        return Objects.requireNonNullElseGet(authSession.get(Constants.AUTH_CODE_VERIFIER_ENCODED), () -> { throw new IllegalArgumentException("[Fetch Access Token] OAuth2 code verifier is missing from session"); });
    }
}
