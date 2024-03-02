# Wau (Where are u?)

![alt text](app/src/main/res/drawable-xxhdpi/wau_icon.png)

## Preface

Here you will find source files to my Senior Project, which I completed during my undergraduate studies at California Polytechnic State University in 2015. Keep in mind that software tools and frameworks have evolved significantly since my work on this, so you may find most of the design and implementation choices to be a bit outdated and certain service integrations to be entirely defunct. Nonetheless, I have decided to upload and archive this project here as a way of sharing and revisitng the work that I have done. The main goal of this project was to broaden my knowledge in software development, while attempting to create something that I and others could find useful. At the time, since mobile applications had quickly became prevalent in our day-to-day, I figured it would be beneficial toward my goals to tune into this area of technology and try my hand at developing an app from the ground up. Between the two main mobile platforms (iOS and Android), both of which I had little to no prior experience working with, I decided to go with Android since I was most familiar with Java, and Android devices were more accessible to me as a broke college student.

## Introduction

Wau (Where are u?) is an Android application that features a friend locator and location-based photo sharing service. The app was originally conceived as a tool that could help streamline the process of locating friends at crowded events and/or unfamiliar locations; a process which may often involve attempts to call and communicate over the phone in noisy environments or deciphering vague directions and landmarks via text. With this app, users can simply tap on a name in their friends list and be presented with that person's geolocation in respect to their own. In addition, friends may proceed to provide a visual reference point of their exact location by capturing and sending temporary photos of their immediate surroundings through the app.

As this idea developed, there was a desire to expand the scope to include a more general or social aspect to the app. Thus, in relation to geolocations and sending photos, the idea of location-based photo sharing was incorporated. This separate feature allows users to capture and upload pictures, publicly or privately, with the caveat being that the only way for anyone to discover or view them is to be within the same vicinity in which those photos were taken. Potential use cases for this would be for scavenger hunts, geocaching, or simply reminiscing on old memories when revisiting a place one used to frequent.

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
