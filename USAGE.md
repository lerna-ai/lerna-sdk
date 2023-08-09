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
    - [Include Lerna SDK from aar file](#include-lerna-sdk-from-aar-file)
    - [Include Lerna SDK from repository](#include-lerna-sdk-from-repository)
    - [Declare Lerna SDK](#declare-lerna-sdk)
    - [Initialize](#initialize)
    - [Start up Lerna](#start-up-lerna)
    - [Notify that the app was stopped](#notify-that-the-app-was-stopped)
    - [Setup/Update User Identification](#setupupdate-user-identification)
    - [Provide custom features to the Library](#provide-custom-features-to-the-library)
    - [Inform the Library of a success event](#inform-the-library-of-a-success-event)
    - [Trigger on demand inference](#trigger-on-demand-inference)
    - [Enable user data upload](#enable-user-data-upload)
  - [Use Lerna SDK Recommendation API](#use-lerna-sdk-recommendation-api)
    - [Get Recommendations](#get-recommendations)
    - [Submit Event to Recommendation Engine](#submit-event-to-recommendation-engine)

## Usage

### Use Lerna SDK

In order to integrate Lerna SDK as Library include library file to your application's dependencies

#### Include Lerna SDK from .aar file

To include the Lerna library, download the latest `.aar` file and place it in the libs folder of your project.

Update your repositories configuration on the `build.gradle` file by adding `flatDir{dirs 'libs'}` line as following example:

```bash
    repositories {
        ...
        flatDir {
            dirs 'libs'
        }
    }
```

Finally, add the Lerna entry on your dependencies in your app `build.gradle` file, as below:

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

#### Include Lerna SDK from repository

To include the Lerna library, from our maven repository follow the steps below. This option requires AWS CLI version 2.0.21 and above.

Add Lerna profile on AWS credentials configuration file located in `~/.aws/credentials` by adding the following lines

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
			url 'https://lerna-dev-470158444867.d.codeartifact.us-east-1.amazonaws.com/maven/lerna-dev/'
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
	--domain lerna-dev \
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

#### Declare Lerna SDK

Use the following line to declare the Lerna instance. This declaration should be added in the class that needs to interact with the Library. For example, for single activity apps, you can add it on top of your main activity class.

```bash
private lateinit var lerna: Lerna
```

#### Initialize

To initialize Lerna SDK ensure that you have a valid token for an ML Application. Add the following lines on the entry point of your respective class. For example, for single activity apps, you can add it on top of your onCreate method of your main activity class.

```bash
lerna = Lerna(
	applicationContext, 
	"your-application-token")
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
lerna.captureEvent(modelName, positionID, successVal, elementID (optional))
```

#### Trigger on demand inference

You can trigger on demand inference process with the following call:

```bash
lerna.triggerInference(modelName, positionID (optional), predictionClass (optional), numElements (optional default 1))
```

#### Enable user data upload

You can enable user data uploading for debug purposes. By default this functionality is enabled.

```bash
lerna.enableUserDataUpload(true)
```

### Use Lerna SDK Recommendation API

In order to use recommendation API, if enabled, you need to follow the bellow steps

#### Get recommendations

All queries are personalized and use the unique user identifier that was configured with [Setup/Update User Identification](#setupupdate-user-identification) function, or auto generated by Lerna Library.

##### Get recommendations for user

Use the following line to get the recommendation list for the mobile user:

```bash
lerna.getRecommendations(modelName)
```

##### Get chosen number of recommendations for user

Use the following line to get the recommendation list with a specific number of items for the mobile user:

```bash
lerna.getRecommendations(modelName, number)
```

##### Get recommendations for user with selected criteria

Use the following line to get the recommendation list based on specific criteria:

```bash
lerna.getRecommendations(modelName, number, blacklistItem, rules)
```
###### Query Parameter Specification

The query fields determine what data are matched when returning recommendations.

* number: max number of recommendations to return. There is no guarantee that this number will be returned for every query.
* blacklistItems: this part of the query specifies individual items to remove from returned recommendations. It can be used to remove duplicates when items are already shown in a specific context. This is called anti-flood in recommender use.
* rules: optional, array of fields values and biases to use in this query.
  * name field name for metadata stored in the EventStore.
  * values an array on one or more values to use in this query. The values will be looked for in the field name.
  * bias will either boost the importance of this part of the query or use it as a filter. Positive biases are boosts any negative number will filter out any results that do not contain the values in the field name.

> NOTE:
>
> The "bias" however picks which of the above types are executed:
>
> bias = -1: Include recommended items that match the rest of the Rule\
> bias = 0: Exclude recommended items that match the rest of the Rule\
> bias > 0: Boost recommended items that match the rest of the Rule by the bias value. This will cause matching recommendations to be moved upward in ranking of returned results.

The response object in all cases is a list of items with the following format

```
[
  {
    "item": "String",
    "score": Float,
    "props": {
      "Key as String 1": ["value1"],
      "Key as String 2": ["value2", "value3", "value4"],
      ...
    }
  }
  ...
]
```

#### Submit Event to Recommendation Engine

To submit a success event to Recommendation engine you just use the same call that capture event to library that describes [here](#inform-the-library-of-a-success-event).

  <p align="center">
    © All Rights Reserved. Lerna Inc. 2022.
  </p>
