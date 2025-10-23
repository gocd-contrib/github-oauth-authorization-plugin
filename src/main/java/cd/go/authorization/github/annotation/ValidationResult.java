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

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static cd.go.authorization.github.utils.Util.GSON;

public class ValidationResult {
    private final Set<ValidationError> errors = new HashSet<>();

    public ValidationResult() {
    }

    public ValidationResult(Collection<ValidationError> errors) {
        this.errors.addAll(errors);
    }

    public void addError(String key, String message) {
        errors.add(new ValidationError(key, message));
    }

    public void addError(@NotNull ValidationError validationError) {
        errors.add(validationError);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String toJSON() {
        return GSON.toJson(errors);
    }

    public boolean hasKey(String key) {
        return errors.stream().anyMatch(validationError -> key.equals(validationError.key()));
    }

    public List<ValidationError> errors() {
        return Collections.unmodifiableList(new ArrayList<>(errors));
    }
}
