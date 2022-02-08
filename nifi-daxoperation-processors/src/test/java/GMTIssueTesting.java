import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class GMTIssueTesting {

    private SimpleDateFormat dateformat  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    public void GMTDateTesting() throws ParseException {
        System.out.println(dateformat.parse(getDateWithoutGMT("2021-04-12 15:56:08")));
    }

    public String getDateWithoutGMT(String dt) {
        int pos = dt.indexOf('T');
        if (pos == -1) {
            dt = dt.replace(' ', 'T');
        }

        pos = dt.indexOf('+');
        if (pos != -1)
            dt = dt.substring(0, dt.indexOf('+'));

        if (dt.endsWith(".0"))
            dt = dt.substring(0, dt.length() - 2);

        //remove milliseconds
        pos = dt.indexOf('.');
        if (pos != -1)
            dt = dt.substring(0, dt.indexOf('.'));


        System.out.println(dt);
        return (dt);
    }
}
