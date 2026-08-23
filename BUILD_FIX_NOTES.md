# Build Fix Notes

The uploaded project contained a corrupted `gradle/wrapper/gradle-wrapper.jar`.
This fixed archive removes the broken JAR and changes GitHub Actions to regenerate a clean wrapper before building.

Additional changes:
- Gradle Actions installs Gradle 9.3.1 directly.
- The wrapper is regenerated with Gradle 9.3.1.
- Build logs use `--info` and `--warning-mode all`.
- KSP is aligned with Kotlin 2.2.10 using `2.2.10-2.0.2`.
