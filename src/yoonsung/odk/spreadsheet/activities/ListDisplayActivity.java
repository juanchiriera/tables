package yoonsung.odk.spreadsheet.activities;

import yoonsung.odk.spreadsheet.data.ColumnProperties;
import yoonsung.odk.spreadsheet.data.DataManager;
import yoonsung.odk.spreadsheet.data.DbHelper;
import yoonsung.odk.spreadsheet.data.Query;
import yoonsung.odk.spreadsheet.data.UserTable;
import yoonsung.odk.spreadsheet.view.ListDisplayView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class ListDisplayActivity extends Activity implements DisplayActivity {
    
    private static final int RCODE_ODKCOLLECT_ADD_ROW = 0;
    
    private DataManager dm;
    private Controller c;
    private Query query;
    private ListViewController listController;
    private UserTable table;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = new Controller(this, this, getIntent().getExtras());
        dm = new DataManager(DbHelper.getDbHelper(this));
        query = new Query(dm.getAllTableProperties(), c.getTableProperties());
        listController = new ListViewController();
        init();
    }
    
    @Override
    public void init() {
        query.clear();
        query.loadFromUserQuery(c.getSearchText());
        table = c.getIsOverview() ?
                c.getDbTable().getUserOverviewTable(query) :
                c.getDbTable().getUserTable(query);
        c.setDisplayView(buildView());
        setContentView(c.getWrapperView());
    }
    
    private View buildView() {
        return ListDisplayView.buildView(this, c.getTableProperties(),
                c.getTableViewSettings(), listController, table);
    }
    
    private void openCollectionView(int rowNum) {
        Query q = new Query(dm.getAllTableProperties(),
                c.getTableProperties());
        for (String prime : c.getTableProperties().getPrimeColumns()) {
            ColumnProperties cp = c.getTableProperties()
                    .getColumnByDbName(prime);
            int colNum = c.getTableProperties().getColumnIndex(prime);
            q.addConstraint(cp, table.getData(rowNum, colNum));
        }
        Controller.launchTableActivity(this, c.getTableProperties(),
                q.toUserQuery(), false);
    }
    
    @Override
    public void onBackPressed() {
        c.onBackPressed();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        switch (requestCode) {
        case RCODE_ODKCOLLECT_ADD_ROW:
            c.addRowFromOdkCollectForm(
                    Integer.valueOf(data.getData().getLastPathSegment()));
            init();
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        c.buildOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return c.handleMenuItemSelection(item.getItemId());
    }
    
    @Override
    public void onSearch() {
        c.recordSearch();
        init();
    }
    
    @Override
    public void onAddRow() {
        Intent intent = c.getIntentForOdkCollectAddRow();
        if (intent != null) {
            startActivityForResult(intent, RCODE_ODKCOLLECT_ADD_ROW);
        }
    }
    
    private void onItemClick(int rowNum) {
        if (c.getIsOverview() &&
                (c.getTableProperties().getPrimeColumns().length > 0)) {
            openCollectionView(rowNum);
        } else {
            Controller.launchDetailActivity(this, c.getTableProperties(),
                    table, rowNum);
        }
    }
    
    private class ListViewController implements ListDisplayView.Controller {
        @Override
        public void onListItemClick(int rowNum) {
            onItemClick(rowNum);
        }
    }
}