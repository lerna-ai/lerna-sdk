<p align="center">
  <a href="https://lerna.ai/">
    <img src="https://dev.lerna.ai/img/Lerna.png" width="200" alt="Lerna AI">
  </a>
  <p align="center">
    Lerna Multiplatform SDK Usage Documentation
    <br/>
    <a href="https://lerna.ai/">lerna.ai</a>
  </p>
</p>

# Lerna Multiplatform SDK Usage Documentation
> Lerna Multiplatform mobile SDK

## Table of contents

- [Usage](#Usage)
  - [Use Lerna SDK](#use-lerna-sdk)
    - [Declare Lerna SDK](#declare-lerna-sdk)
    - [Initialize](#initialize)
    - [Start up Lerna](#start-up-lerna)
    - [Notify that the app was stopped](#notify-that-the-app-was-stopped)
    - [Setup/Update User Identification](#setupupdate-user-identification)
    - [Provide custom features to the Library](#provide-custom-features-to-the-library)
    - [Inform the Library of a success event](#inform-the-library-of-a-success-event)
    - [Trigger on demand inference](#trigger-on-demand-inference)
    - [Enable user data upload](#enable-user-data-upload)

## Usage

### Use Lerna SDK

In order to integrate Lerna SDK as Library include library file to your application's dependencies

#### Include Lerna SDK 

TBD

#### Declare Lerna SDK

Use the following line to declare the Lerna instance. This declaration should be added in the class that needs to interact with the Library. For example, for single activity apps, you can add it on top of your main activity class.

```bash
private lateinit var lerna: Lerna
```

#### Initialize

To initialize Lerna SDK ensure that you have a valid token for an ML Application. Add the following lines on the entry point of your respective class. For example, for single activity apps, you can add it on top of your onCreate method of your main activity class.

You can also include custom features on your ML by adding the number of columns as `customFeaturesSize`. Default value is `0`

```bash
lerna = Lerna(
	applicationContext, 
	"your-application-token", 
	customFeaturesSize = "number of custom features as integer",
	autoInference = "flag to enable/disable auto inference")
```

> NOTE:
>
> You also need to replace `your-application-token` with the application token that you received upon registration.

#### Start up Lerna

To start up the Library use the following line:

```bash
lerna.start()
```

> NOTE:
>
> To use the Library inside the activity's lifecycle you can add this line inside `onResume()` method of the activity.
>
> ```bash
> override fun onResume() {
>     super.onResume()
>     lerna.start()
> }
> ```

#### Notify that the app was stopped

In order to notify the Library for your application lifecycle end you need to use the following line:

```bash
lerna.stop()
```

> NOTE:
>
> To use the Library inside the activity's lifecycle with foreground service enabled you can add this line inside `onDestroy()` method of the activity.
>
> ```bash
> override fun onDestroy() {
>     super.onDestroy()
>     lerna.stop()
> }
> ```
>
> In case you operate without the foreground service, you can add this line in the place that you want to stop the Library. For example, in order to stop the Library when another activity comes into the foreground, add the respective line inside the `onPause()` method of the activity.
>
> ```bash
> override fun onPause() {
>     super.onPause()
>     lerna.stop()
> }
> ```

#### Setup/Update User Identification

You can define a unique ID for each device by running the following command. This should be used if you have a user identification service in order to identify users on received success events.

```bash
lerna.setUserIdentifier("Unique user identifier as string")
```

#### Provide custom features to the Library

If you wish to feed custom features to the Library, you have to submit an Array of Float values to the Library every time that you need to update it. To make this update, you need to store the values in an Array of floats that you declare in your activity as shown on the following line:

```bash
val features = FloatArray("number of custom features as integer")
```

As a next step, you need to update the Array based on your requirements; for example, you can use the following code for the purpose of a proximity sensor update on place zero of the features array:

```bash
override fun onSensorChanged(event: SensorEvent?) {
    if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
        features[0] = event.values[0]
    }
}
```

Finally, you can use the following code to send the updated values to the Library:

```bash
lerna.updateFeature(features)
```

#### Inform the Library of a success event

Use the following line to submit a success event to the Library:

```bash
lerna.captureEvent()
```

The Library provides different success events, which are identified by positive integers with the following call:

```bash
lerna.captureEvent("event number as integer")
```

#### Trigger on demand inference

You can trigger on demand inference process with the following call:

```bash
lerna.triggerInference()
```

#### Enable user data upload

You can enable user data uploading for debug purposes. By default this functionality is enabled.

```bash
lerna.enableUserDataUpload(true)
```

  <p align="center">
    © All Rights Reserved. Lerna Inc. 2022.
  </p>