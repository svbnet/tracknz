package co.svbnet.tracknz.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.ConnectException;
import java.util.List;

import co.svbnet.tracknz.R;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.tasks.PackageRetrievalTask;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;
import co.svbnet.tracknz.ui.ToolbarActivity;
import co.svbnet.tracknz.util.CodeValidationUtil;
import co.svbnet.tracknz.util.EasterEggUtil;

public class CodeInputActivity extends ToolbarActivity {

    public static final String CODE = "code";

    private static final String EASTER_EGG_TEXT = "FORTUNE666";

    private EditText codeText, labelText;
    private String code;
    private CheckBox notificationsCheck;
    private MenuItem trackItem;

    private TrackingDB db = new TrackingDB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAndToolbar(R.layout.activity_code_input);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if (intent.hasExtra(CODE)) {
            code = intent.getStringExtra(CODE);
        }
        setupUi();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        validate(codeText.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_add_code, menu);
        trackItem = menu.findItem(R.id.action_track);
        if (code == null) trackItem.setEnabled(false);
        return true;
    }

    private void validate(String s) {
        if (!CodeValidationUtil.isValidCode(s)) {
            codeText.setError(getString(R.string.error_code_invalid));
            if (trackItem != null) trackItem.setEnabled(false);
        } else {
            if (trackItem != null) trackItem.setEnabled(true);
        }
    }

    private void setupUi() {
        codeText = (EditText)findViewById(R.id.code);
        codeText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        codeText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        codeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validate(s.toString());
            }
        });
        codeText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    validate(((EditText)v).getText().toString());
                }
            }
        });
        labelText = (EditText)findViewById(R.id.label);
        labelText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (trackItem != null && trackItem.isEnabled() && actionId == EditorInfo.IME_ACTION_DONE) {
                    submitPackage();
                    return true;
                }
                return false;
            }
        });
        notificationsCheck = (CheckBox)findViewById(R.id.package_notifications);
        if (code != null) {
            // If we are supplied with a code, insert it into the code edittext and focus on the label
            codeText.append(code);
            labelText.requestFocus();
            if (trackItem != null) trackItem.setEnabled(true);
        }
        validate(codeText.getText().toString());
    }

    private void submitPackage() {
        String code = codeText.getText().toString().trim();
        if (db.doesPackageExist(code)) {
            new AlertDialog.Builder(CodeInputActivity.this)
                    .setTitle(R.string.title_error)
                    .setMessage(R.string.message_code_already_exists)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            new AddSinglePackageTask(new NZPostTrackingService()).execute(code);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_track:
                if (codeText.getText().toString().equals(EASTER_EGG_TEXT)) {
                    Toast.makeText(this, EasterEggUtil.newFortune(), Toast.LENGTH_LONG).show();
                } else {
                    submitPackage();
                }
                break;

        }
        return true;
    }

    private class AddSinglePackageTask extends PackageRetrievalTask {

        private ProgressDialog progressDialog;

        public AddSinglePackageTask(NZPostTrackingService service) {
            super(service);
            progressDialog = new ProgressDialog(CodeInputActivity.this);
            progressDialog.setMessage(getString(R.string.message_getting_package_information));
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            // Hide keyboard before showing progress dialog
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isAcceptingText() && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<NZPostTrackedPackage> trackedPackages) {
            progressDialog.dismiss();
            progressDialog = null;
            super.onPostExecute(trackedPackages);
        }

        @Override
        protected void onException(Exception ex) {
            String errorMessage = getString(R.string.message_unknown_error,
                    ex.getClass().getName(), ex.getMessage());
            if (ex instanceof ConnectException) {
                errorMessage = getString(R.string.message_error_no_connection);
            }
            new AlertDialog.Builder(CodeInputActivity.this)
                    .setTitle(R.string.title_error)
                    .setMessage(errorMessage)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        @Override
        protected void onSuccess(List<NZPostTrackedPackage> retrievedPackages) {
            // Only one package being retrieved
            final NZPostTrackedPackage retrievedPackage = retrievedPackages.get(0);
            // Create intent for Package info activity
            final Intent intent = new Intent();
            intent.putExtra(MainActivity.CURRENT_PACKAGE, retrievedPackage);
            String label = labelText.getText().toString();
            if (!label.isEmpty()) retrievedPackage.setLabel(label);
            if (retrievedPackage.getErrorCode() != null) {
                // Package is valid but doesn't exist in NZ Post system
                if (retrievedPackage.getErrorCode().equals("N")) {
                    new AlertDialog.Builder(CodeInputActivity.this)
                            .setTitle(R.string.title_nzp_error)
                            .setMessage(getString(R.string.message_add_nonexistent_package, retrievedPackage.getTrackingCode()))
                            .setPositiveButton(R.string.dialog_button_yes_add, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.insertPackage(retrievedPackage);
                                    dialog.dismiss();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.dialog_button_dont_add, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    finish();
                                }
                            })
                            .show();
                } else {
                    new AlertDialog.Builder(CodeInputActivity.this)
                            .setTitle(R.string.title_nzp_error)
                            .setMessage(retrievedPackage.getDetailedStatus())
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNeutralButton(R.string.dialog_button_add_anyway, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.insertPackage(retrievedPackage);
                                    dialog.dismiss();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            })
                            .show();
                }
            } else {
                db.insertPackage(retrievedPackage);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}
