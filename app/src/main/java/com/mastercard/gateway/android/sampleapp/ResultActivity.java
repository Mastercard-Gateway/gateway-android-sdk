package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Show a screen which displays a payment status
 */
public class ResultActivity extends AbstractActivity {
    @Bind ( R.id.resultView )
    LinearLayout view;

    @Bind ( R.id.resultText )
    TextView resultText;

    @Bind ( R.id.resultExplanation )
    TextView resultExplanation;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        int colour = getIntent().getIntExtra( "COLOUR", R.color.in_progress_bg );
        String text = getIntent().getStringExtra( "TEXT" );
        String explanation = getIntent().getStringExtra( "EXPLANATION" );

        view.setBackgroundColor( ContextCompat.getColor( getBaseContext(), colour ) );
        resultText.setText( text != null ? text : "" );
        resultExplanation.setText( explanation != null ? explanation : "" );
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_result;
    }

    @OnClick ( R.id.continueBtn )
    protected void doContinue() {
        Intent intent = new Intent( this, MainActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
    }
}
