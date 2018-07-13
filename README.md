# GitHub OAuth authorization plugin for GoCD

The plugin allows user to login in GoCD using GitHub(or GitHub Enterprise). It also supports authorization, which can be used by server admin to map GoCD roles with GitHub organizations or teams.

# Installation

Documentation for installation is available [here](INSTALL.md)

# Capabilities

* The plugin is implemented using `GoCD plugin authorization endpoint`. Hence, it supports `authentication` and `authorization`.

## Troubleshooting

Add environment variable `GO_SERVER_SYSTEM_PROPERTIES=-Dplugin.cd.go.authorization.github.log.level=debug` to enable debug log. The plugin logs are written to `LOG_DIR/plugin-cd.go.authorization.github.log`. The log dir on Linux is `/var/log/go-server`; logs on Windows are written to `C:\Program Files\Go Server\logs`; logs on docker images are written to `/godata/logs`.


## Building the code base

To build the jar, run `./gradlew clean test assemble`

## License

```plain
Copyright 2017 ThoughtWorks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
