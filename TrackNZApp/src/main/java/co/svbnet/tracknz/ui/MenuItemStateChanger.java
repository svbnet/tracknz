package co.svbnet.tracknz.ui;

import android.view.Menu;
import android.view.MenuItem;

/**
 * A helper class that changes the visibility/enabled state of many menu items in an activity.
 */
public class MenuItemStateChanger {

    private final int[] IDS;
    private MenuItem[] itemReferences;

    /**
     * Creates a new instance with the specified menu item resource IDs. It is safe to call the
     * constructor in your activity's onCreate or constructor as it does not require a menu to be
     * initialised.
     * @param ids An array of menu item resource IDs.
     */
    public MenuItemStateChanger(int[] ids) {
        IDS = ids;
        itemReferences = new MenuItem[ids.length];
    }

    /**
     * Acquires {@link MenuItem} references from the IDs and menu specified.
     * @param menu The menu which the menu item IDs belong to.
     */
    public void assign(Menu menu) {
        for (int i = 0; i < IDS.length; i++) {
            itemReferences[i] = menu.findItem(IDS[i]);
        }
    }

    public MenuItem[] getItemReferences() {
        return itemReferences;
    }

    public void setItemsEnabled(boolean enabled) {
        for (MenuItem item : itemReferences) {
            if (item != null) {
                item.setEnabled(enabled);
            }
        }
    }

    public void setItemsVisible(boolean visible) {
        for (MenuItem item : itemReferences) {
            if (item != null) {
                item.setVisible(visible);
            }
        }
    }


}
