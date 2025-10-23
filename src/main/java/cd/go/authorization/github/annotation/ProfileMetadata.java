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

package cd.go.authorization.github.annotation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Optional;

public class ProfileMetadata<T extends Metadata> {

    @Expose
    @SerializedName("key")
    private String key;

    @Expose
    @SerializedName("metadata")
    private T metadata;

    public ProfileMetadata(String key, T metadata) {
        this.key = key;
        this.metadata = metadata;
    }

    public Optional<ValidationError> validate(String input) {
        return isRequired() && (input == null || input.isBlank())
                ? Optional.of(new ValidationError(key, this.key + " must not be blank."))
                : Optional.empty();
    }

    public String getKey() {
        return key;
    }

    public boolean isRequired() {
        return metadata.isRequired();
    }
}