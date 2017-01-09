package tm16wiki.wikidefine;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.AsyncTask;
import android.text.Editable;
import android.util.Log;

import tm16wiki.wikidefine.helperClasses.https;
import tm16wiki.wikidefine.helperClasses.xml;
import tm16wiki.wikidefine.wikiAPI.wikiTextParser;

/**
 * Created by harryk on 1/9/17.
 *@author Harry K.
 *
 *
 * A Proxy Class to manage The Backend, In Singleton design (for better Managing)
 *
 * I'm not that good with Threading and such, so if i break it here please inform me but i just Reimplemented the Algorithm
 * This way we can have only one Object to manage Loading of queries,
 *          We can encapsulate the (Somewhat Cryptic) Algorithms
 *          Separate UIspecific Code from Application LOgic
 *
 *          I love Design Patterns ;)
 */

public class WikiProxy extends BaseObservable{
    /**
     * URL To retrieve the content
     */
    private final static String URL = "https://en.wikipedia.org/wiki/Special:Export/";
    /**
     * Text field
     */
    private final static String TEXT = "text";

    /**
     * Required length for proper definition i suppose
     */
    private final static int REQ_LENGTH = 30;

    private static final String TAG = "DebugTag";

    /**
     * Exception Thrown when we dont have ant results
     */
    private class NotFoundException extends Exception{
        NotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Exception Thrown when we dont Have any Connection (https.loadurl(..) returns false)
     */
    private class HttpsConnectionException extends Exception{
        HttpsConnectionException(String message) {
            super(message);
        }
    }

    /**
     * A Listener Interface For Checking for results when we have a connection
     * Might be handy with Progress Bars ,etc
     */
    interface RetrieverListener {
        void onLoad();
        void onFinish();
        void onReceiveException(String exceptionMessage);
    }

    /**
     * References to Composited objects
     */
    private static WikiProxy sWikiProxy;
    private tm16wiki.wikidefine.helperClasses.xml XmlHelper;
    private tm16wiki.wikidefine.helperClasses.https HttpsHelper ;
    private wikiTextParser wikiTextParser;
    private RetrieverListener mRetrieverListener;
    private String result;

    /**
     * private Constructor, because Singleton
     */
    private WikiProxy() {
        wikiTextParser = new wikiTextParser();
        HttpsHelper = new https();
        XmlHelper = new xml();
        result = " ";
    }

    /**
     * A getter method returning the current result
     * bindable annotation makes databinding monitor changes, No more Findviewbyid :)
     * @return current result of last succesful query
     */
    @Bindable
    public String getResult(){
        return result;
    }

    /**
     * set Result from succesful query
     *
     * @param result  query result
     */
    public void setResult(String result){
        this.result = result;
        notifyPropertyChanged(tm16wiki.wikidefine.BR.result);
    }

    /**
     * Implement to listen for results
     * @param mRetrieverListener Callback interface
     */
    void setmRetrieverListener(RetrieverListener mRetrieverListener) {
        this.mRetrieverListener = mRetrieverListener;
    }

    /**
     * How we Obtain our Singleton Proxy Object
     *
     * @return  a reference to our Static object if present
     */
    static WikiProxy getWikiProxy(){
        if(sWikiProxy == null){
            sWikiProxy = new WikiProxy();
        }
        return sWikiProxy;
    }

    /**
     * Call to get The content for parsing
     * @param editable from getText method of editext
     * @return The definitions
     * @throws NotFoundException When the result is not there
     * @throws HttpsConnectionException When theres no connection
     */
    private String retrieveContent(Editable editable) throws NotFoundException, HttpsConnectionException {
        if(HttpsHelper.loadURL(URLBuilder(editable))){
            String definition = wikiTextParser.getDefinition(XmlHelper.getTagValue(HttpsHelper.getContent(),TEXT));
            if(definition.length() < REQ_LENGTH){
                throw  new NotFoundException("Not Found");
            }else {
                return definition;
            }
        }else {
            throw new HttpsConnectionException("No Connection");
        }
    }

    /**
     * AsyncTask that loads the information in a simple background thread without blocking UI,
     * We Can Implement a HandlerThread if you want more Flexibility, But for now , this Works
     */
    class ContentLoader extends AsyncTask<Void,Void,String>{
        private final Editable ed;
        private boolean isRethrowException;
        private String exceptionMessage = null;

        /**
         * Pass your query here
         * @param param content to query
         */
        ContentLoader(Editable param) {
            ed = param;
            isRethrowException = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(mRetrieverListener != null){
                mRetrieverListener.onLoad();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return retrieveContent(ed);
            } catch (NotFoundException | HttpsConnectionException e) {
                exceptionMessage = e.getMessage();
                isRethrowException = true;
            }
            return " ";
        }


        @Override
        protected void onPostExecute(String definition) {
            super.onPostExecute(definition);
            if(mRetrieverListener != null){
                mRetrieverListener.onFinish();
            }
            if(isRethrowException) {
                if (mRetrieverListener != null) {
                    mRetrieverListener.onReceiveException(exceptionMessage);
                }
            }
            setResult(SanitizeDefinition(definition));
        }


    }



    /**
     * Last section of that Algorithm separated for clarity
     * @param def String to sanitize
     * @return Sanitized string
     */
    private String SanitizeDefinition(String def){
        while (def.indexOf(" ")==0){
            def =def.substring(1, def.length());
        }
        Log.d(TAG, "SanitizeDefinition: " + def);
        return def.replace("*", "\n*");
    }

    /*
    this was the original method, decomposed within this class

    public void do_it(){
        if(https.loadURL(URLBuilder(searchtext.getText()))){
            String content = xml.getTagValue(https.getContent(),TEXT);
            String definition = text.getDefinition(content);
            if(definition.length() < REQ_LENGTH ){
                Snackbar.make(view, R.string.not_found, Snackbar.LENGTH_LONG).show();
            }else {
                while (definition.indexOf(" ")==0){
                    definition =definition.substring(1, definition.length());
                }
                textview.setText(definition.replace("*", "\n*"));
            }
        }else{
            Snackbar.make(view, R.string.not_found, Snackbar.LENGTH_LONG).show();

        }
    }*/

    /**
     * Returns a Concatenated full URL component, Can add extra sanitizer of input
     * @param query extra part
     * @return full URL
     */
    private String URLBuilder(Editable query){
        return URL + query;
    }



}
