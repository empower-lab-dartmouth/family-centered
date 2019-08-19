package com.example.myfirstapp;

import android.view.MenuItem;

//the actual quest activities use BasicFunctionalityPart2
// the entering information/getting set up activities use BasicFunctionality
// this class makes "home" BeginQuest instead of IntroSequenceActivity2

public class BasicFunctionalityPart2 extends BasicFunctionality{

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            startActivityAfterCleanup(BeginQuest.class); //can be changed to have home button take user to a different point
            return true;
        }
        return super.onOptionsItemSelected(item);}
}
