# Github oauth authorization plugin for GoCD

## Requirements

* GoCD server version v17.5.0 or above
* GitHub oauth application's `ClientId` and `ClientSectret`
* GitHub `personal access token` to access private organizations and teams 

## Installation

Copy the file `build/libs/github-authorization-plugin-VERSION.jar` to the GoCD server under `${GO_SERVER_DIR}/plugins/external` 
and restart the server. The `GO_SERVER_DIR` is usually `/var/lib/go-server` on Linux and `C:\Program Files\Go Server` 
on Windows.

## Configuration

###  Create GitHub oauth application

1. Login into your GitHub account
2. Navigate to [Developer applications](https://github.com/settings/developers)
3. Click **Register a new application**.
4. In "Application name", type the name of your app.
5. In "Authorization callback URL", type `https://your.goserver.url/go/plugin/cd.go.authorization.github/authenticate`
6. Click **Register application**.
7. Note down the `Client ID` and `Client Secret` of your application.

### Create GitHub personal access token

1. [Verify your email address](https://help.github.com/articles/verifying-your-email-address/), if it hasn't been verified yet.
2. Navigate to [Personal access tokens](https://github.com/settings/tokens).
3. Click **Generate new token**.
4. Give a name to your token.
5. In scope, select following values:
    - `read:org` to read org and team membership
    - `read:user` to read all user profile data
    - `user:email` to access user email addresses (read-only)
6. Click **Generate token**.
7. Copy the generated token and save it. 


    Note: After you navigate off the page, the token will not be visible due to security reasons. 

### Create Authorization Configuration

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Security_** _>_ **_Authorization Configuration_**
2. Click on **_Add_** to create new authorization configuration
    1. Specify `id` for auth config
    2. Select `GitHub authorization plugin for GoCD` for **_Plugin id_**
    3. Choose `GitHub` or `GitHub Enterprise` for `Authenticate with`.
    5. Specify **_Client ID_** and **_Client Secret_**
    6. Specify **_Personal access token_**
    7. Save your configuration
    
### Create Role Configuration

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Security_** _>_ **_Role Configuration_**   
2. Click on **_Add_** to create new role configuration
    1. Specify `name` for your role
    2. Select `Auth Config Id` of previously created [authorization configuration](#create-authorization-configuration)
    3. Specify `GitHub organization's` name for **_GitHub organizations_**  
        - If user is a member of at least one organization specified here, then plugin will assign this role to user
        - Must be provided as comma-separated values
    4. Specify `GitHub Teams` name for **_GitHub teams_** 
        - If user is a member of at least one of the organization team, then plugin will assign this role to user
        - Must be provided in `OrganizationName: TeamA, TeamB ... TeamN` format
    5. Specify `username` of GitHub users for `GitHub users`
        - If user's username is listed here, then plugin will assign this role to user
        - Must be provided as comma-separated values             