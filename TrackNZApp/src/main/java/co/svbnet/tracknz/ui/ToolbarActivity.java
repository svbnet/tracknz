package co.svbnet.tracknz.ui;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import co.svbnet.tracknz.R;

/**
 * A base activity which applies the main toolbar as the action bar and inflates a main app-wide menu.
 */
public abstract class ToolbarActivity extends AppCompatActivity {
    /**
     * A reference to the main activity toolbar. If you need a reference to the toolbar as an action bar,
     * call getSupportActionBar().
     */
    protected Toolbar toolbar;

    /**
     * Sets the activity's content view and sets the main toolbar to the support action bar. Be sure
     * to call this instead of setContentView or else the toolbar will not load.
     * @param layoutResID The layout resource ID of a layout to load.
     */
    public void setContentViewAndToolbar(int layoutResID) {
        setContentView(layoutResID);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Inflates and adds the main app menu. Make sure to call this first or else the main menu
     * won't appear.
     * @param menu The activity menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app, menu);
        return true;
    }
}
