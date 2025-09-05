# Release Notes
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


[1.1.9-beta01] - 2025-09-04
### Added
- Enabled support for Browser Payment flow, including Ottu integration.
- Added a loader inside WebView to show progress until content is fully loaded.
- Added full Kotlin support in the SDK and sample application.

### Changed
- Refactored minimal SDK code to support both 3DS and Browser Payment flows.
- Replaced Gateway3DSecureCallback with unified GatewayCallback.
- Refactored and partially rewrote the sample application.
- Updated Gradle plugin to com.android.tools.build:gradle:8.4.0.
- Increased minSdkVersion from 19 → 21.
- Cleaned up code related to JCenter.
- Updated and added new unit tests.

## [1.1.8] - 2025-03-25
### Added
- Updated Gson library to version 2.12.1 to address security vulnerabilities. 

## [1.1.7] - 2025-02-21
### Added
- DigiCert updated. New Expiry Jan 15, 2038

## [1.1.6] - 2024-02-08
### Added
- Saudi region (KSA) URL

## [1.1.5] - 2022-12-28
### Changed
- Pinned certificate updated. New Expiry December 2030

## [1.1.4] - 2020-03-26
### Fixed
- Issue where WebView was not displaying the 3DS HTML on apps targeting API >=29
### Changed
- SDK and sample app now targeting API 29
- Migrated from legacy Android support libraries to Jetpack

## [1.1.3] - 2020-02-14
### Added
- China region (CN) URL
### Changed
- Enabled TLSv1.2 support for API <21

## [1.1.2] - 2020-02-04
### Added
- India region (IN) URL