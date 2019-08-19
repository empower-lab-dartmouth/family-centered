/*
 * This activity tracks device location in order to display points of interest near the user's
 * current location.  Discovered "caches" stay visible and display name and address.
 *
 * Plan for future: show nearby points of interest based on google's Places SDK.  Integrate with
 * Wikipedia API to give information about cache points?
 *
 * Instructions for if it doesn't seem to be working on start up:
 *      Check versions of google APIs in the .gradle files under "dependencies".  Fiddling with these sometimes helps
 *
 * Adjusting device location in the emulator:
 *      While the device emulator is active, click the "..." at the bottom of the control panel to open the
 *      Extended Controls. You can adjust the Latitude and Longitude here and update it in the emulator by clicking send.
 */

//package edu.stanford.googlemapsptest;
package com.example.myfirstapp;

//8
public class MapsActivityQ4 extends MapsActivity2 {
    @Override
    public void testingMoveOn(){
        moveOnInner(Quest4Activity.class);
    }
}

