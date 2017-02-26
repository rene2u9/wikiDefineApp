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
 *
 * @author Harry K.
 *         <p>
 *         <p>
 *         A Proxy Class to manage The Backend, In Singleton design (for better Managing)
 *         <p>
 *         I'm not that good with Threading and such, so if i break it here please inform me but i just Reimplemented the Algorithm
 *         This way we can have only one Object to manage Loading of queries,
 *         We can encapsulate the (Somewhat Cryptic) Algorithms
 *         Separate UIspecific Code from Application LOgic
 *         <p>
 *         I love Design Patterns ;)
 */

public class WikiProxy extends BaseObservable {
    /**
     * URL To retrieve the content
     */
    private final static String URL = "wikipedia.org/wiki/Special:Export/";
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
     *
     */
    private static String LANG = "en";
    /**
     * References to Composited objects
     */
    private static WikiProxy sWikiProxy;
    private tm16wiki.wikidefine.helperClasses.xml XmlHelper;
    private tm16wiki.wikidefine.helperClasses.https HttpsHelper;
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
     * How we Obtain our Singleton Proxy Object
     *
     * @return a reference to our Static object if present
     */
    static WikiProxy getWikiProxy() {
        if (sWikiProxy == null) {
            sWikiProxy = new WikiProxy();
        }
        return sWikiProxy;
    }

    /**
     * A getter method returning the current result
     * bindable annotation makes databinding monitor changes, No more Findviewbyid :)
     *
     * @return current result of last succesful query
     */
    @Bindable
    public String getResult() {
        return result;
    }

    /**
     * set Result from succesful query
     *
     * @param result query result
     */
    public void setResult(String result) {
        this.result = result;
        notifyPropertyChanged(tm16wiki.wikidefine.BR.result);
    }

    /**
     * Implement to listen for results
     *
     * @param mRetrieverListener Callback interface
     */
    void setmRetrieverListener(RetrieverListener mRetrieverListener) {
        this.mRetrieverListener = mRetrieverListener;
    }

    /**
     * Call to get The content for parsing
     *
     * @param editable from getText method of editext
     * @return The definitions
     * @throws NotFoundException        When the result is not there
     * @throws HttpsConnectionException When theres no connection
     */
    private String retrieveContent(Editable editable) throws NotFoundException, HttpsConnectionException {
        if (HttpsHelper.loadURL(URLBuilder(editable))) {
            String definition = wikiTextParser.getDefinition(XmlHelper.getTagValue(HttpsHelper.getContent(), TEXT));
            if (definition.length() < REQ_LENGTH) {

                throw new NotFoundException("Not Found");
            } else {
                return definition;
            }
        } else {
            throw new HttpsConnectionException("No Connection");
        }
    }




    private String retrieveContent(String editable) throws NotFoundException, HttpsConnectionException {
        if (HttpsHelper.loadURL("https://" + LANG + "." + URL + editable)) {
            String definition = wikiTextParser.getDefinition(XmlHelper.getTagValue(HttpsHelper.getContent(), TEXT));
            if (definition.length() < REQ_LENGTH) {

                throw new NotFoundException("Not Found");
            } else {
                return definition;
            }
        } else {
            throw new HttpsConnectionException("No Connection");
        }
    }






    /**
     * Last section of that Algorithm separated for clarity
     *
     * @param def String to sanitize
     * @return Sanitized string
     */
    private String SanitizeDefinition(String def) {
        while (def.indexOf(" ") == 0) {
            def = def.substring(1, def.length());
        }
        Log.d(TAG, "SanitizeDefinition: " + def);
        return def.replace("*", "\n*");
    }

    /**
     * Returns a Concatenated full URL component, Can add extra sanitizer of input
     *
     * @param query extra part
     * @return full URL
     */
    private String URLBuilder(Editable query) {
        return "https://" + LANG + "." + URL + query;
    }

    protected void setLang(String lang) {
        LANG = lang;
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
     * Exception Thrown when we dont have ant results
     */
    private class NotFoundException extends Exception {
        NotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Exception Thrown when we dont Have any Connection (https.loadurl(..) returns false)
     */
    private class HttpsConnectionException extends Exception {
        HttpsConnectionException(String message) {
            super(message);
        }
    }
















    /**
     * AsyncTask that loads the information in a simple background thread without blocking UI,
     * We Can Implement a HandlerThread if you want more Flexibility, But for now , this Works
     */
    class ContentLoader extends AsyncTask<Void, Void, String> {
        private Editable ed;
        private String query;
        private boolean isRethrowException;
        private String exceptionMessage = null;

        /**
         * Pass your query here
         *
         * @param param content to query
         */
        ContentLoader(Editable param) {
            ed = param;
            isRethrowException = false;
        }

        ContentLoader(String query){
            this.query = query;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mRetrieverListener != null) {
                mRetrieverListener.onLoad();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return retrieveContent(query);
            } catch (NotFoundException | HttpsConnectionException e) {
                exceptionMessage = e.getMessage();
                isRethrowException = true;
            }
            return " ";
        }

        @Override
        protected void onPostExecute(String definition) {
            super.onPostExecute(definition);
            if (mRetrieverListener != null) {
                mRetrieverListener.onFinish();
            }
            if (isRethrowException) {
                if (mRetrieverListener != null) {
                    mRetrieverListener.onReceiveException(exceptionMessage);
                }
            }
            setResult(SanitizeDefinition(definition));
        }
    }

}
