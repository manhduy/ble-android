# BleCentral library

BleCentral is a wrapper library for easy establishing a BLE Central. 
This library uses Kotlin coroutine so it is the better to use it in [`ViewModel`](https://developer.android.com/topic/libraries/architecture/viewmodel?gclid=CjwKCAjwwYP2BRBGEiwAkoBpApXMMi5ZjEaCjqBX-wOaZz3KlQLKL65x2fdPWHZTnpCY2QuHgVMUZBoCwS4QAvD_BwE&gclsrc=aw.ds).

## Getting started

### Setting up the dependency

The first step is to include BleCentral in your project. Create a ./libs folder in your app module and copy blelib.aar to this folder.
Then add this line to gradle file.

```groovy
implementation fileTree(dir: 'libs', include: ['blelib.aar'])
```

### Create BleCentral instance

```kotlin
var bleCentral: BleCentral = BleCentral(app, this)
```

### Start the BLE Central

You can start BLE Central normally:

```kotlin
bleCentral.start()
```

But it is better to use viewModelScope

```kotlin
viewModelScope.launch {
    withContext(Dispatchers.IO) {
        bleCentral.start()
    }
}
```

### Start the BLE Central

Stop Ble Central when it is no longer using. It is the best practice to stop BLE Central in onDestroy of activity.

```kotlin
bleCentral.start()
```

### [Library Document](android-blelib-docs/index.html)