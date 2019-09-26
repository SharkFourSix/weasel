### What

This application allows you to download music from Malawi Music website

### How

The application automatically indexes songs discovered on the website as the user browses the 
website using the embedded web browser. The user then has  the option to download the indexed songs to their Android device.

### Notes on releases

- [x] Release tags must reflect the application version name (i.e v1.0) as defined in [build.gradle](app/build.gradle)
- [x] There must be at most 3 releases at any given moment and no more
- [x] APK name must me "app-release.apk" under each respective release tag

### Contributions

Pull requests are welcome or derive your own version so long as the packages don't conflict.

### Keystore

Why is the keystore included?

Good question. The keystore is password protected and since ONLY I can sign any releases from
this repository, there's no need to worry about anything.

Those that want to release their own version will simply have to sign and release using their
own certificate.