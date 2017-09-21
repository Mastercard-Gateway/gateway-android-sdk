package com.mastercard.gateway.android.sampleapp;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.mastercard.gateway.android.sampleapp.databinding.ActivityResultBinding;

/**
 * Show a screen which displays a payment status
 */
public class ResultActivity extends AbstractActivity {

    ActivityResultBinding binding;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        binding = DataBindingUtil.setContentView(this, R.layout.activity_result);

        int colour = getIntent().getIntExtra( "COLOUR", R.color.in_progress_bg );
        String text = getIntent().getStringExtra( "TEXT" );
        String explanation = getIntent().getStringExtra( "EXPLANATION" );

        binding.resultView.setBackgroundColor( ContextCompat.getColor( getBaseContext(), colour ) );
        binding.resultText.setText( text != null ? text : "" );
        binding.resultExplanation.setText( explanation != null ? explanation : "" );

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doContinue();
            }
        });
    }

    protected void doContinue() {
        Intent intent = new Intent( this, MainActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
    }
}
