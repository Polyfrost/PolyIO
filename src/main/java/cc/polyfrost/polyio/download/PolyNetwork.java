package cc.polyfrost.polyio.download;

import cc.polyfrost.polyio.PolyIO;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author xtrm
 */
class PolyNetwork {
    private static final int NETWORK_TIMEOUT = 15_000;

    private PolyNetwork() {
    }

    public static URLConnection createConnection(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(NETWORK_TIMEOUT);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(false);
        urlConnection.setReadTimeout(NETWORK_TIMEOUT);
        {
            urlConnection.setRequestProperty("Accept", "*/*");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            urlConnection.setRequestProperty("Connection", "keep-alive");
            urlConnection.setRequestProperty("Host", url.getHost());
            urlConnection.setRequestProperty("Referer", url.toString());
            urlConnection.setRequestProperty("User-Agent", PolyIO.USER_AGENT);
        }
        return urlConnection;
    }
}
