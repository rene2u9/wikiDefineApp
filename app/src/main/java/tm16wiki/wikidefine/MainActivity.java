package tm16wiki.wikidefine;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import tm16wiki.wikidefine.wikiAPI.wikiTextParser;

public class MainActivity extends AppCompatActivity {

    private EditText searchtext;
    private TextView textview;


    tm16wiki.wikidefine.helperClasses.xml xml = new tm16wiki.wikidefine.helperClasses.xml();
    tm16wiki.wikidefine.helperClasses.https https = new tm16wiki.wikidefine.helperClasses.https();
    wikiTextParser text = new wikiTextParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchtext = (EditText) findViewById(R.id.editText);
        textview = (TextView) findViewById(R.id.outputTextView);
        textview.setMovementMethod(new ScrollingMovementMethod());

        View.OnClickListener action = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "lade...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                if(https.loadURL("https://en.wikipedia.org/wiki/Special:Export/"+ searchtext.getText())){
                    String content = xml.getTagValue(https.getContent(), "text");
                    String definition = text.getDefinition(content);
                    if(definition.length() < 30){
                        Snackbar.make(view, "nichts gefunden ¯\\_(ツ)_/¯", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    }else {
                        while (definition.indexOf(" ")==0){
                            definition =definition.substring(1, definition.length());
                        }
                        textview.setText(definition.replace("*", "\n*"));
                    }
                }else{
                    Snackbar.make(view, "nichts gefunden ¯\\_(ツ)_/¯", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }


            }
        };


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(action);
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
}
