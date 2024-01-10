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

- [Usage of Lerna SDK](#usage-of-lerna-sdk)
  - [Lerna SDK as aar file](#lerna-sdk-as-aar-file)
  - [Lerna SDK from the repository](#lerna-sdk-from-the-repository)
  - [Declaring the Lerna SDK instance](#declaring-the-lerna-sdk-instance)
  - [Initialization](#initialization)
  - [Lerna start-up](#lerna-start-up)
  - [Notification that the app has stopped](#notification-that-the-app-has-stopped)
  - [Setup/Update of User Identification](#setupupdate-of-user-identification)
  - [Custom features](#custom-features)
  - [Input item data](#input-item-data)
  - [Informing the Library of a success event](#informing-the-library-of-a-success-event)
  - [Configuring auto inference](#configuring-auto-inference)
  - [Triggering on demand inference](#triggering-on-demand-inference)
  - [Triggering on demand inference with input data](#triggering-on-demand-inference-with-input-data)
  - [Refreshing session](#refreshing-session)
  - [Enabling user data upload](#enabling-user-data-upload)
- [Usage of Lerna SDK Recommendation API](#usage-of-lerna-sdk-recommendation-api)
  - [Getting Recommendations](#getting-recommendations)
  - [Submitting an Event to the Recommendation Engine](#submitting-an-event-to-the-recommendation-engine)

<div style="page-break-after: always;"></div>

## Usage of Lerna SDK

In order to integrate the Lerna SDK as a Library include the library file to your application's dependencies

### Lerna SDK as .aar file

To include the Lerna library, download the latest `.aar` file and place it in the libs folder of your project.

Update your repositories configuration on the `build.gradle` file by adding `flatDir{dirs 'libs'}` line as follows:

```bash
    repositories {
        ...
        flatDir {
            dirs 'libs'
        }
    }
```

Finally, add the Lerna entry on your app's dependencies in the `build.gradle` file, as shown below:

```bash
dependencies {
    implementation(name:'lerna-kmm-sdk-release', ext:'aar')
}
```

and the required dependencies of the library, if they do not already exist, as seen below:

```bash
dependencies {
	...
    implementation("org.jetbrains.kotlinx:multik-core:0.2.1")
    implementation("org.jetbrains.kotlinx:multik-kotlin:0.2.1")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-network:$ktorVersion")
    implementation("io.ktor:ktor-network-tls:$ktorVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
    implementation("io.github.aakira:napier:2.6.1")
    implementation("com.soywiz.korlibs.korio:korio:$korioVersion")
    runtimeOnly("io.ktor:ktor-utils:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
}
```

### Lerna SDK from the repository

To include the Lerna library from our maven repository follow the steps below. Note that this option requires AWS CLI version 2.0.21 and above.

Add the Lerna profile on the AWS credentials configuration file located in `~/.aws/credentials` by adding the following lines

```bash
[lerna]
aws_access_key_id = <your access key>
aws_secret_access_key = <your secret key>
```

Update your repositories configuration on the `build.gradle` file by adding our `maven` repository as follows:

```bash
	repositories {
		...
		maven {
			url 'https://lerna-ai-470158444867.d.codeartifact.us-east-1.amazonaws.com/maven/release/'
			credentials {
				username "aws"
				password System.env.LERNA_CODEARTIFACT_AUTH_TOKEN
			}
		}
	}
```
Get the authorization token to access the repository by running the command

```bash
export LERNA_CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token \
	--profile lerna \
	--domain lerna-ai \
	--domain-owner 470158444867 \
	--region us-east-1 \
	--query authorizationToken \
	--output text`
```

Finally, add the Lerna entry on your dependencies in your app's `build.gradle` file, as shown below:

```bash
dependencies {
    implementation 'ai.lerna.multiplatform:lerna-kmm-sdk-android:0.0.1'
}
```

### Declaring the Lerna SDK instance

Use the following line to declare the Lerna instance. This declaration should be added in the class that needs to interact with the Library. For example, for single activity apps, you can add it on top of your main activity class.

```bash
private lateinit var lerna: Lerna
```

### Initialization

To initialize the Lerna SDK, first ensure that you have a valid token for an ML Application. Add the following lines on the entry point of your respective class. For example, for single activity apps, you can add it on top of your onCreate method of your main activity class.

```bash
lerna = Lerna(
	applicationContext, 
	String application_token)
```

> NOTE:
>
> You also need to replace `application_token` with the application token that you received upon registration.

### Lerna start-up 

To start the Library use the following line:

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

### Notification that the app has stopped

In order to notify the Library for your application's lifecycle end you need to use the following line:

```bash
lerna.stop()
```

> NOTE:
>
> To use the Library inside the activity's lifecycle with foreground service enabled, you can add this line inside `onDestroy()` method of the activity.
>
> ```bash
> override fun onDestroy() {
>     super.onDestroy()
>     lerna.stop()
> }
> ```
>
> In case your app does not use the foreground service, you can add this line in the place that you want to stop the Library. For example, in order to stop the Library when another activity comes into the foreground, add the respective line inside the `onPause()` method of the activity.
>
> ```bash
> override fun onPause() {
>     super.onPause()
>     lerna.stop()
> }
> ```

### Setup/Update of User Identification

You can define a unique ID for each device by running the following command. This should be used if you have a user identification service in order to identify your users when receiving success events.

```bash
lerna.setUserIdentifier(String userID)
```

### Custom features

If you wish to feed your custom features to the Library, you have to submit an Array of Float values to it every time that you need to update them. To do this, you need first to store the values in an Array of floats that you declare in your activity as shown in the following line:

```bash
val features = FloatArray(int no_custom_features)
```

Next, you need to update the Array based on your requirements; for example, you can use the following code for the purpose of a proximity sensor update in the first position of the features array:

```bash
override fun onSensorChanged(event: SensorEvent?) {
    if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
        features[0] = event.values[0]
    }
}
```

Finally, you can use the following code to send the updated values to the Library:

```bash
lerna.updateFeature(Float[] features)
```

### Input item data

The Lerna library provides two ways to submit input item data to Lerna. The first option is to use a dedicated call for the input data, and the second is to make a combined call that provides the data and requests the inference.
In the sequel, we first explain the first option, and then the second one.

As a first step, you need to provide a unique `itemID` of the item that you submit the input data for.

You need to create an array of Float values based on your requirements; for example, you can use the provided converter to convert your own structure to an array of features:

```bash
val inputData = LernaConverter.convert.apply(myStructure myObject)
```

You also need to define the `positionID` of the item, this parameter is helpful in case that you have multiple positions in your app's UI.

Finally, you can use the following code to send the input data to the Library:

```bash
lerna.addInputData(String itemID, Float[] inputData, String positionID)
```

### Informing the Library of a success event

Use the following line to submit a success event to the Library:

```bash
lerna.captureEvent(String modelName, String positionID, String successVal, String elementID (optional))
```

### Configuring auto inference

You can configure the auto inference mechanism for a specific model with some limitations:
- you cannot have multiple positions, meaning you can have auto inference in general, not for different UI elements
- you can use the custom features, but not additional metadata tight to an item/position

You can configure the auto inference mechanism with the following call:

```bash
lerna.setAutoInference(String modelName, String setting)
```

The `setting` parameter should have either the value `on` or `off`.

> NOTE:
>
> The auto inference should be enabled only for one model

### Triggering on demand inference

You can trigger on demand inference with the following command:

```bash
lerna.triggerInference(String modelName, String positionID (optional), String predictionClass (optional), int numElements (optional default 1))
```

> NOTE:
>
> The parameters on this call is
> modelName: The name of the ML model
> positionId: (Optional) The position of the items, in case that you have multiple positions in your app's UI
> predictionClass: (Optional) The class that you request the inference for (like, comment, etc.)
> numElements: (Optional) The number of elements that you need for the position

### Triggering on demand inference with input data

To use trigger inference while providing the input data at the same time, you need first to create a map with input data as follow:

```bash
val inputDataMap = mutableMapOf<String, FloatArray>()
for (myObject in myObjectList) {
    inputDataMap[myObject.id] = LernaConverter.convert.apply(myStructure myObject)
}

```

You can trigger the on demand inference with input data with the following call:

```bash
lerna.triggerInference(Map inputDataMap, String modelName, String positionID (optional), String predictionClass (optional), int numElements (optional default 1))
```
<div style="page-break-after: always;"></div>

### Refreshing session

You need to inform Lerna when the user refreshes the session; for example, when they request more items. You can use the following call:

```bash
lerna.refresh(String modelName)
```

### Enabling user data upload

You can enable user data uploading for debug purposes. By default this functionality is enabled.

```bash
lerna.enableUserDataUpload(true)
```

<div style="page-break-after: always;"></div>
## Usage of Lerna SDK Recommendation API

In order to use the recommendation API, if needed, you need to follow the steps below.

### Getting recommendations

All queries are personalized and use the unique user identifier that was configured with [Setup/Update User Identification](#setupupdate-user-identification) function, or auto generated by Lerna Library.

#### Getting recommendations for a user

Use the following line to get the recommendation list for the mobile user:

```bash
lerna.getRecommendations(String modelName)
```

#### Getting specific number of recommendations for a user

Use the following line to get a specific number of recommendation items for the mobile user:

```bash
lerna.getRecommendations(String modelName, int number)
```

[//]: # (#### Get recommendations for a user with selected criteria)

[//]: # (Use the following line to get the recommendation list based on specific criteria:)

[//]: # (```bash)
[//]: # (lerna.getRecommendations&#40;String modelName, int number, List blacklistItem, Array rules&#41;)
[//]: # (```)

[//]: # (#### Query Parameter Specification)

[//]: # (The query fields determine what data are matched when returning recommendations.)
[//]: # (* number: max number of recommendations to return. There is no guarantee that this number will be returned for every query.)
[//]: # (* blacklistItems: this part of the query specifies individual items to remove from returned recommendations. It can be used to remove duplicates when items are already shown in a specific context. This is called anti-flood in recommender use.)
[//]: # (* rules: optional, array of fields values and biases to use in this query.)
[//]: # (  * name field name for metadata stored in the EventStore.)
[//]: # (  * values an array on one or more values to use in this query. The values will be looked for in the field name.)
[//]: # (  * bias will either boost the importance of this part of the query or use it as a filter. Positive biases are boosts any negative number will filter out any results that do not contain the values in the field name.)

[//]: # (> NOTE:)
[//]: # (>)
[//]: # (> The "bias" however picks which of the above types are executed:)
[//]: # (>)
[//]: # (> bias = -1: Include recommended items that match the rest of the Rule\)
[//]: # (> bias = 0: Exclude recommended items that match the rest of the Rule\)
[//]: # (> bias > 0: Boost recommended items that match the rest of the Rule by the bias value. This will cause matching recommendations to be moved upward in ranking of returned results.)

[//]: # (The response object in all cases is a list of items with the following format)

[//]: # (```)
[//]: # ([)
[//]: # (  {)
[//]: # (    "item": "String",)
[//]: # (    "score": Float,)
[//]: # (    "props": {)
[//]: # (      "Key as String 1": ["value1"],)
[//]: # (      "Key as String 2": ["value2", "value3", "value4"],)
[//]: # (      ...)
[//]: # (    })
[//]: # (  })
[//]: # (  ...)
[//]: # (])
[//]: # (```)

### Submitting an Event to the Recommendation Engine

To submit a success event to the Recommendation engine you use the same call that captures the event in the library as described [here](#inform-the-library-of-a-success-event).

<div style="height: 400px"></div>
  <p align="center">
    © All Rights Reserved. Lerna Inc. 2023.
  </p>
