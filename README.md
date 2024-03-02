# Wau (Where are u?)

![alt text](app/src/main/res/drawable-xxhdpi/wau_icon.png)

## Preface

This is a senior project which I completed during my undergraduate studies at California Polytechnic State University in 2015. Though some of the design and implementation choices are a bit outdated, and certain service integrations are entirely defunct now, I have decided to upload and archive this project as a way of sharing and revisiting the work that I have done. The original goal of this project was to broaden my knowledge in software development while attempting to create something that I and others would find useful. At the time, since mobile applications had quickly became prevalent in our day-to-day, I figured it would be beneficial to tune into this area of technology and try my hand at developing an app from the ground up. Between the two main mobile platforms, iOS and Android, both of which I had little to no prior experience developing on, I decided to work with Android since I was most familiar with Java, and Android devices were more accessible to me as a broke college student.

## Introduction

Wau (Where are u?) is an Android application that features a friend locator and location-based photo-sharing service. This app was originally conceived as a tool that can help streamline the process of locating friends at crowded events and/or unfamiliar locations; a process which may often involve attempts to call and communicate over the phone in noisy environments or deciphering vague directions and landmarks via text. With this app, users can simply tap on a name in their friends list and be presented with that person's geolocation in relation to their own. In addition, friends may proceed to provide a visual reference point of their exact location by capturing and sending temporary photos of their immediate surroundings through the app.

As this was developed, there was a desire to expand the scope to include a more general or social aspect to the app. Thus in relation to geolocations and sending photos, the idea of location-based photo-sharing was incorporated. This separate feature allows users to capture and upload photos, publicly or privately, with the caveat being that the only way for others to discover or view these pictures is to be within the same vicinity in which they were taken. Potential use cases for this would be for scavenger hunts, geocaching, or simply reminiscing on old memories when visiting a place one used to frequent.

## Prerequisites

* Android 4.0+
* [Google API key](http://developers.google.com/maps/documentation/android/) (geolocation services)
   * update value in [AndroidManifest.xml](app/src/main/AndroidManifest.xml)
* [Parse API key](http://parse.com/docs/android/api/) (BaaS)
   * update values in [strings.xml](app/src/main/res/values/strings.xml)

## Tools

* [Android Studio](https://developer.android.com/studio)
* [Action Bar Style Generator](http://jgilfelt.github.io/android-actionbarstylegenerator/)

## Reference Material

* [Android API docs](http://developer.android.com/reference/packages.html)
* [Android friend list and photo capturing tutorial](http://teamtreehouse.com/library/build-a-selfdestructing-message-android-app)
* [Android image loading tutorial](http://www.technotalkative.com/android-load-images-from-web-and-caching/)
* [Android login/signup tutorial](http://www.androidbegin.com/tutorial/android-parse-com-simple-login-and-signup-tutorial/)
