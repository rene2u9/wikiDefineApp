package tm16wiki.wikidefine;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import javax.net.ssl.SNIServerName;

import tm16wiki.wikidefine.databinding.ActivityMainBinding;
import tm16wiki.wikidefine.wikiAPI.wikiTextParser;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";

    /**
     * i'll replace these, A Databinding component will handle them, Makes Code decoupled and less :)
     */
    private WikiProxy wikiProxy;
    private ActivityMainBinding activityMainBinding;

    /**
     * @author: Harry Kituyi
     * A Handler for our Proxy from the UI
     */
    public  class MainActivityHandler {
        private final Context mAppContext;

        /**
         * Constructor for the databinding handler
         *
         * @param context pass an activity for some methods that may need it
         */
        MainActivityHandler(Context context) {
            mAppContext = context;
        }

        public void query(View view){
             Editable editable = activityMainBinding.contentMainLayout.editText.getText();
            if(TextUtils.isEmpty(editable)){
                Snackbar.make(view,R.string.insert_query,Snackbar.LENGTH_SHORT).show();
            }else {
                    activityMainBinding.getWikiProxy().new ContentLoader(editable).execute();
            }
        }




    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        setContentView(R.layout.activity_main);
        All the magic happens below
         */
        activityMainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        wikiProxy = WikiProxy.getWikiProxy();
        wikiProxy.setmRetrieverListener(new WikiProxy.RetrieverListener() {
            @Override
            public void onLoad() {
                Log.d(TAG, "onLoad: Loading");
                activityMainBinding.contentMainLayout.contentProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish: Done loading");
                activityMainBinding.contentMainLayout.contentProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceiveException(String exceptionMessage) {
                Snackbar.make(activityMainBinding.getRoot(),
                        getResources().getString(R.string.not_found),
                        Snackbar.LENGTH_SHORT).show();
            }
        });
        activityMainBinding.setWikiProxy(wikiProxy);
        activityMainBinding.setMainActivityHandler(new MainActivityHandler(this));
        activityMainBinding.contentMainLayout.outputTextView.setMovementMethod(new ScrollingMovementMethod());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
