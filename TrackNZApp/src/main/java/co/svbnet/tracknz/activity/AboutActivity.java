package co.svbnet.tracknz.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import co.svbnet.tracknz.BuildConfig;
import co.svbnet.tracknz.R;
import co.svbnet.tracknz.ui.ToolbarActivity;

public class AboutActivity extends ToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAndToolbar(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((TextView)findViewById(R.id.version_label)).setText(BuildConfig.VERSION_NAME);
        findViewById(R.id.review_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=co.svbnet.tracknz")));
            }
        });
        findViewById(R.id.website_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://svbnet.co")));
            }
        });
        findViewById(R.id.credits_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AboutActivity.this)
                        .setTitle(R.string.btn_credits)
                        .setMessage(getString(R.string.about_credits))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }
}
