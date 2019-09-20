package com.didichuxing.tools.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


/**
 * Dummy activity for test
 */
public class MainActivity extends Activity implements IInterface.Callback<String> {
    public static final String TAG = "MainActivity";

    static {
        //Test with static initializer block
        Log.d(TAG, "static initializer: ");
    }

    public MainActivity() {
        //Test with constructor block
        Log.d(TAG, "constructor: ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button button = new Button(this);

        //Replace test

        Log.d(TAG, "onCreate: ");//test method call replace

        ExampleSpec example = new ExampleSpec(0);
        String name = ExampleSpec.name; //field get
        ExampleSpec.name = "test";//field set

        // Insert test
        example.run(); //method call

        //test inner class

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //method exec
                Log.d(TAG, "onClick: ");
            }
        });
        ExampleSpec example2 = new ExampleSpec();
        int id = example2.id;//field get
        example2.id = 1;//field set

        // Around test
        example2.call();

        ExampleSpec example3 = new ExampleSpec("test");
        int id2 = example3.id2;//field get
        example3.id2 = 2;//field set

        //Enhance

        //Try catch test
        startActivity(new Intent());//will crash here, test crash
        //Timing test
        ExampleSpec test4 = new ExampleSpec(1, 2);
        test4.timing();

        onCallback("");

        Child.main(null);
    }

    @Override
    public void onCallback(String value) {
        System.out.println("onCallback");
    }
}
