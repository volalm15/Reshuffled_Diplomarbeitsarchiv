package serial.requests;

import data.config.data.ConfigModel;
import data.config.service.Config;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import jssc.SerialPort;
import serial.Serial;
import logging.Logger;
import main.Main;

/**
 *
 * @author volalm15
 */
public abstract class Request {

    private static final Logger LOG = Logger.getLogger(Request.class.getName());

    private static final java.util.zip.CRC32 CRC32 = new java.util.zip.CRC32();
    private static final DateFormat DATAFORMATTER = new SimpleDateFormat("mm:ss.S");

    private String reqFrame;              // frame bytes sent to µC (only text)
    private byte[] resFrame;             // frame bytes received from µC
    private final long timeMillisCreatedAt;     // epoch time when this object is created
    private long timeMillisFrameSent;     // epoch time when frame is sent to µC
    private long timeMillisFrameReceived; // epoch time ehne frame from µC is received 


    public Request() {
        timeMillisCreatedAt = System.currentTimeMillis();
    }

    public void handleRequestSent() throws IOException {
        timeMillisFrameSent = System.currentTimeMillis();

    }

    public void handleResponse(byte[] receivedResponse) throws IOException {
      timeMillisFrameReceived = System.currentTimeMillis();
    }

    protected void createRequestFrame(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException();
        }
        CRC32.reset();
        CRC32.update(content.getBytes());
        final String crc32 = String.format("%08X", CRC32.getValue());

        final StringBuilder sb = new StringBuilder(128);
        sb.append(':').append(content).append('#').append(crc32).append('\n');
        reqFrame = sb.toString();
    }

    public String getMreqFrame() {
        return reqFrame;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("Request").append('{');
        sb.append("created=").append(DATAFORMATTER.format(timeMillisCreatedAt));

        sb.append(", mreqFrame='");
        for (int i = 0; i < reqFrame.length(); i++) {
            final char c = reqFrame.charAt(i);
            if (c >= ' ' && c <= '~') {
                sb.append(c);
            } else if (c == '\n') {
                sb.append("\\n");
            } else {
                sb.append("\\").append(Character.getNumericValue(c));
            }
        }
        sb.append("'");
        if (timeMillisFrameSent > 0) {
            sb.append(", sentTime=+").append((timeMillisFrameSent - timeMillisCreatedAt)).append("ms");
        }
        if (timeMillisFrameReceived > 0) {
            sb.append(", sentTime=+").append((timeMillisFrameReceived - timeMillisCreatedAt)).append("ms");
        }

        if (resFrame != null) {
            sb.append(", mresFrame='");
            for (byte b : resFrame) {
                if (b >= 32 && b < 127) {
                    sb.append((char) b);
                } else if (b == '\n') {
                    sb.append("\\n");
                } else {
                    sb.append("\\").append(b < 0 ? (int) b + 256 : (int) b);
                }
            }
        }
        sb.append('}');

        return sb.toString();
    }

    public long getTimeMillisCreatedAt() {
        return timeMillisCreatedAt;
    }

    public long getTimeMillisFrameSent() {
        return timeMillisFrameSent;
    }

    public long getTimeMillisFrameReceived() {
        return timeMillisFrameReceived;
    }
    

}
