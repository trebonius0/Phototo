package photato;

import java.net.SocketTimeoutException;
import org.apache.http.ConnectionClosedException;
import org.apache.http.ExceptionLogger;

public class StdErrorExceptionLogger implements ExceptionLogger {

    @Override
    public void log(final Exception ex) {
        if (ex instanceof SocketTimeoutException || ex instanceof ConnectionClosedException) {
            // Do nothing
        } else {
            ex.printStackTrace();
        }
    }

}
