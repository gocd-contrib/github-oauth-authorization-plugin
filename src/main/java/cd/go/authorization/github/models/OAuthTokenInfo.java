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

package cd.go.authorization.github.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static cd.go.authorization.github.utils.Util.GSON;

public class OAuthTokenInfo {

    @Expose
    @SerializedName("access_token")
    private String oauthAccessToken;
    @Expose
    @SerializedName("token_type")
    private String tokenType;

    @Expose
    @SerializedName("scope")
    private String scope;

    public OAuthTokenInfo() {}

    public OAuthTokenInfo(String oauthAccessToken, String tokenType, String scope) {
        this.oauthAccessToken = oauthAccessToken;
        this.tokenType = tokenType;
        this.scope = scope;
    }

    public String oauthAccessToken() {
        return oauthAccessToken;
    }

    public String tokenType() {
        return tokenType;
    }

    public String scope() {
        return scope;
    }

    public String toJSON() {
        return GSON.toJson(this);
    }

    public static OAuthTokenInfo fromJSON(String json) {
        return GSON.fromJson(json, OAuthTokenInfo.class);
    }
}
