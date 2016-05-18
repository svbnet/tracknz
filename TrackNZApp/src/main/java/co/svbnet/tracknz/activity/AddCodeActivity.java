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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import co.svbnet.tracknz.BuildConfig;
import co.svbnet.tracknz.R;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.tasks.PackageRetrievalTask;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackedPackage;
import co.svbnet.tracknz.tracking.nzpost.NZPostTrackingService;
import co.svbnet.tracknz.ui.ToolbarActivity;
import co.svbnet.tracknz.util.CodeValidationUtil;

@Deprecated
public class AddCodeActivity extends ToolbarActivity {

    private LinearLayout packageEntryLayout;
    private List<EditText> entryEditTextList = new ArrayList<>();
    private Button addCodeButton;
    private TrackingDB db = new TrackingDB(this);
    private int dialogsLeft = 0;

    private static final int REQUEST_BARCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewAndToolbar(R.layout.activity_add_code);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (BuildConfig.DEBUG) {
            Intent intent = getIntent();
            if (!intent.hasExtra("co.svbnet.tracknz.DEBUG_DUMMY_CODES")) {
                return;
            }
            String[] codes = intent.getStringArrayExtra("co.svbnet.tracknz.DEBUG_DUMMY_CODES");
            new RetrievePackagesTask(new NZPostTrackingService()).execute(codes);
        }
        setupUi();
        addTextViewEntry(null);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_code, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    private void setupUi() {
        packageEntryLayout = (LinearLayout)findViewById(R.id.package_entry_layout);
        addCodeButton =(Button)findViewById(R.id.add_code_entry);
        addCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTextViewEntry(null);
            }
        });
    }

    private boolean canAddTextViewEntry() {
        return CodeValidationUtil.isValidCode(entryEditTextList.get(entryEditTextList.size() - 1).getText().toString());
    }

    private void addTextViewEntry(String code) {
        if (entryEditTextList.size() == 1 && code != null) {
            EditText editText = entryEditTextList.get(0);
            if (editText.getText().length() == 0) {
                // .append() puts the cursor after the text
                editText.append(code);
                return;
            }

        }
        final EditText editEntry = new EditText(this);
        editEntry.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        editEntry.setHint(R.string.hint_enter_a_code);
        editEntry.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        if (code != null) {
            editEntry.append(code);
        }
        packageEntryLayout.addView(editEntry, packageEntryLayout.getChildCount());
        editEntry.requestFocus();
        entryEditTextList.add(editEntry);
        editEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!CodeValidationUtil.isValidCode(s.toString())) {
                    editEntry.setError(getString(R.string.error_code_invalid));
                }
                // If the user has cleared all text and that EditText isn't the only one
                if (s.toString().isEmpty() && entryEditTextList.size() > 1) {
                    packageEntryLayout.removeView(editEntry);
                    entryEditTextList.remove(editEntry);
                    // Request focus for EditText before the one just removed
                    entryEditTextList.get(entryEditTextList.size() - 1).requestFocus();
                }
                // Only allow adding another if the last edittext has text in it
                addCodeButton.setEnabled(canAddTextViewEntry());
            }
        });
        addCodeButton.setEnabled(canAddTextViewEntry());
    }

    private List<String> getContentsOfAllEditors() {
        List<String> codes = new ArrayList<>();
        for (EditText item : entryEditTextList) {
            String contents = item.getText().toString();
            // Strip out spaces and make uppercase
            // uppercase and replace rhyme lol
            contents = contents.replace(" ", "").toUpperCase();
            if (!contents.isEmpty()) {
                codes.add(contents);
            }
        }
        return codes;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_track:
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isAcceptingText()) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                List<String> codes = getContentsOfAllEditors();
                if (codes.size() == 0) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.title_error)
                            .setMessage(R.string.message_no_codes_entered)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    return true;
                }
                new RetrievePackagesTask(new NZPostTrackingService()).execute(codes.toArray(new String[codes.size()]));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private class RetrievePackagesTask extends PackageRetrievalTask {

        private ProgressDialog progressDialog;

        public RetrievePackagesTask(NZPostTrackingService service) {
            super(service);
            progressDialog = new ProgressDialog(AddCodeActivity.this);
            progressDialog.setMessage(getString(R.string.message_getting_package_information));
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onException(Exception ex) {
            String errorMessage = getString(R.string.message_unknown_error,
                    ex.getClass().getName(), ex.getMessage());
            if (ex instanceof ConnectException) {
                errorMessage = getString(R.string.message_error_no_connection);
            }
            new AlertDialog.Builder(AddCodeActivity.this)
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
        protected void onPostExecute(List<NZPostTrackedPackage> trackedPackages) {
            progressDialog.hide();
            super.onPostExecute(trackedPackages);
        }

        private void finishIfNoDialogs() {
            if (dialogsLeft <= 0) {
                setResult(RESULT_OK);
                finish();
            }
        }

        @Override
        protected void onSuccess(List<NZPostTrackedPackage> retrievedPackages) {
            for (final NZPostTrackedPackage retrievedPackage : retrievedPackages) {
                if (retrievedPackage.getErrorCode() != null) {
                    dialogsLeft++;
                    if (retrievedPackage.getErrorCode().equals("N")) {
                        new AlertDialog.Builder(AddCodeActivity.this)
                                .setTitle(R.string.title_nzp_error)
                                .setMessage(getString(R.string.message_add_nonexistent_package, retrievedPackage.getTrackingCode()))
                                .setPositiveButton(R.string.dialog_button_yes_add, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        db.insertPackage(retrievedPackage);
                                        dialogsLeft--;
                                        dialog.dismiss();
                                        finishIfNoDialogs();
                                    }
                                })
                                .setNegativeButton(R.string.dialog_button_dont_add, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialogsLeft--;
                                        dialog.cancel();
                                        finishIfNoDialogs();
                                    }
                                })
                                .show();
                    } else {
                        new AlertDialog.Builder(AddCodeActivity.this)
                                .setTitle(R.string.title_nzp_error)
                                .setMessage(retrievedPackage.getDetailedStatus())
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialogsLeft--;
                                        dialog.dismiss();
                                        finishIfNoDialogs();
                                    }
                                })
                                .show();
                    }
                } else {
                    db.insertPackage(retrievedPackage);
                }
            }
            finishIfNoDialogs();
        }
    }
}
