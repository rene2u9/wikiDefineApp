package tm16wiki.wikidefine;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.ArrayList;
import tm16wiki.wikidefine.Adapter.MyRecyclerViewAdapter;
import tm16wiki.wikidefine.Adapter.DefinitionTouchHelper;
import tm16wiki.wikidefine.databinding.ActivityMainBinding;
import tm16wiki.wikidefine.wikiAPI.Definition;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /**
     * i'll replace these, A Databinding component will handle them, Makes Code decoupled and less :)
     */
    private WikiProxy wikiProxy;
    private ActivityMainBinding activityMainBinding;


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    FloatingSearchView mSearchView;


    @Override
    protected void onResume() {
        super.onResume();
        ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
            //todo open wiki link?!
            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")));
            }
        });
    }

    private ArrayList<Definition> getDataSet() {
        ArrayList results = new ArrayList<Definition>();
        for (int index = 0; index < 20; index++) {
            Definition obj = new Definition("Some Primary Text " + index,
                    "Secondary " + index);
            results.add(index, obj);
        }
        return results;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        setContentView(R.layout.activity_main);
        All the magic happens below
         */
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        wikiProxy = WikiProxy.getWikiProxy();
        wikiProxy.setmRetrieverListener(new WikiProxy.RetrieverListener() {
            @Override
            public void onLoad() {
                Log.d(TAG, "onLoad: Loading");
                mSearchView.setEnabled(false);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish: Done loading");
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

        //set up cards for definitions
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerViewAdapter(new ArrayList<Definition>());
        mRecyclerView.setAdapter(mAdapter);

        //setup swype to delete
        ItemTouchHelper.Callback callback = new DefinitionTouchHelper((MyRecyclerViewAdapter) mAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecyclerView);


        //searchbar
        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        /*mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {

            }
        });
        */
        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener(){
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {       }

            @Override
            public void onSearchAction(String currentQuery) {
                try {
                    AsyncTask<Void, Void, String> def =  activityMainBinding.getWikiProxy().new ContentLoader(currentQuery).execute();
                    ((MyRecyclerViewAdapter) mAdapter).addItem(
                            new Definition(currentQuery, def.get()) , 0);
                    mRecyclerView.smoothScrollToPosition(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }  );

        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                onOptionsItemSelected(item);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.setLang_de:
                wikiProxy.setLang("de");
                break;
            case R.id.setLang_en:
                wikiProxy.setLang("en");
                break;
            case R.id.setLang_fr:
                wikiProxy.setLang("fr");
                break;
            case R.id.setLang_es:
                wikiProxy.setLang("es");
                break;
            case R.id.setLang_ru:
                wikiProxy.setLang("ru");
                break;
            case R.id.setLang_it:
                wikiProxy.setLang("it");
                break;
            case R.id.setLang_pl:
                wikiProxy.setLang("pl");
                break;
            case R.id.setLang_pt:
                wikiProxy.setLang("pt");
                break;
            case R.id.setLang_zh:
                wikiProxy.setLang("zh");
                break;
            case R.id.setLang_ja:
                wikiProxy.setLang("ja");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }






    /**
     * @author: Harry Kituyi
     * A Handler for our Proxy from the UI
     */
    public class MainActivityHandler {
        private final Context mAppContext;

        /**
         * Constructor for the databinding handler
         *
         * @param context pass an activity for some methods that may need it
         */
        MainActivityHandler(Context context) {
            mAppContext = context;
        }

        public void query(View view) {
          /*  activityMainBinding.contentMainLayout.editText.setText( mSearchView.getQuery() );

            Editable editable = activityMainBinding.contentMainLayout.editText.getText();
            if (TextUtils.isEmpty(editable)) {
                Snackbar.make(view, R.string.insert_query, Snackbar.LENGTH_SHORT).show();
            } else {
                try {
                    AsyncTask<Void, Void, String> def =  activityMainBinding.getWikiProxy().new ContentLoader(editable).execute();
                    ((MyRecyclerViewAdapter) mAdapter).addItem(
                            new Definition(activityMainBinding.contentMainLayout.editText.getText().toString(),
                            def.get()
                            ) , 0);
                    mRecyclerView.smoothScrollToPosition(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            */
        }

    }

}
