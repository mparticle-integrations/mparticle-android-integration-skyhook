## Skyhook Kit Integration

This repository contains the [Skyhook Context Accelerator](http://www.skyhookwireless.com/products/context-accelerator) integration for the [mParticle Android SDK](https://github.com/mParticle/mparticle-android-sdk).

### Adding the integration

1. Add the kit dependency to your app's build.gradle:

    ```groovy
    dependencies {
        compile 'com.mparticle:android-skyhook-kit:4+'
    }
    ```
2. Follow the mParticle Android SDK [quick-start](https://github.com/mParticle/mparticle-android-sdk), then rebuild and launch your app, and verify that you see `"SkyhookKit detected"` in the output of `adb logcat`.
3. Reference mParticle's integration docs below to enable the integration.

### Documentation

Check out our [SDK Documentation](http://docs.mparticle.com/#mobile-sdk-guide) site to learn more.

### License

[Skyhook Terms and Conditions](https://my.skyhookwireless.com/termsofservice).
