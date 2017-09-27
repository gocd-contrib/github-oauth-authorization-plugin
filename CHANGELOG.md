## 2.0.0 - 2017-09-27

### Added
   
- Allow admin to choose authorization using `PersonalAccessToken` or `UserAccessToken`
	- **PersonalAccessToken:** requires an additional permission `read:org` to authorize user based on role configuration.
	- **UserAccessToken:** requires an additional `read:org` permission from user. This allows plugins to access private org and teams of user.	

### Fixed

- Role assignment based on team membership.
- Security fix for cross login issue with v1.0.0 of the plugin due to provider caching at plugin side. 

## 1.0.0 - 2017-07-25

Initial release of plugin
