package co.svbnet.tracknz.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import co.svbnet.tracknz.R;
import co.svbnet.tracknz.data.TrackingDB;
import co.svbnet.tracknz.tracking.TrackedPackage;

/**
 * Contains utility methods that assist in providing a user interface for common operations like editing
 * and deletion of packages.
 */
public class PackageModifyUtil {

    public interface LabelEditComplete {
        void onLabelEditComplete(String newLabel);
    }

    public interface PackageDeleteComplete {
        void onPackageDeleteComplete();
    }

    /**
     * Edits a package's label with a dialog where the user can enter a code.
     * @param context The context to display the dialog in.
     * @param db A DB instance.
     * @param trackedPackage The package to modify.
     * @param callback Called when the OK button on the dialog is pressed and the new label is updated in the database.
     */
    public static void editLabel(Context context, final TrackingDB db, final TrackedPackage trackedPackage, final LabelEditComplete callback) {
        final EditText labelText = new EditText(context);
        labelText.setHint(R.string.hint_no_label);
        labelText.setText(trackedPackage.getLabel() != null ? trackedPackage.getLabel() : "");
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.title_edit_label, trackedPackage.getCode()))
                .setView(labelText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newLabel = labelText.getText().toString();
                        if (newLabel.isEmpty()) {
                            newLabel = null;
                        }
                        trackedPackage.setLabel(newLabel);
                        db.updatePackageLabel(trackedPackage.getCode(), newLabel);
                        callback.onLabelEditComplete(newLabel);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}
