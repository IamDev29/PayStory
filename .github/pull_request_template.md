## Description

Please include a clear, concise summary of the changes introduced in this PR. Explain what issue it resolves or what feature it implements, and any high-level design decisions you've made.

Resolves # (issue number)

## Type of Change

Please delete options that are not relevant.

- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Refactoring (restructuring code without functional changes)
- [ ] Documentation update (improving or expanding docs)
- [ ] Test addition / improvement (adding Robolectric or UI tests)

## How Has This Been Tested?

Describe the tests you ran to verify your changes. Provide details of your local JVM unit tests or screen verification tests.

- [ ] **Unit Tests**: Ran `gradle :app:testDebugUnitTest` and all tests passed.
- [ ] **Screenshot Verification**: Verified that Roborazzi screenshot baselines are clean (`gradle :app:verifyRoborazziDebug`).
- [ ] **Manual Device Testing**: Tested on Android device or emulator running Android version [e.g. 14 / API 34].

## Checklist

- [ ] My code follows the code style guidelines of this project.
- [ ] I have updated/edited `metadata.json` if there was a corresponding change in `strings.xml`.
- [ ] All interactive elements in my UI changes have touch targets of at least `48.dp x 48.dp`.
- [ ] I have added appropriate Compose `testTag` modifiers on interactive UI components.
- [ ] I have commented my code, particularly in hard-to-understand areas.
- [ ] My changes generate no new warnings or build errors.
- [ ] I have updated the documentation (`docs/` or `README.md`) to reflect these changes.
- [ ] I have added/updated unit tests to verify my changes.

## Screenshots (if applicable)

Please attach screenshots, GIFs, or video recordings showing the UI in action (for both Compact and Expanded screen sizes, if relevant).
