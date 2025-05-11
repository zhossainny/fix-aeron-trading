import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class AeronOrderSender {
    public static final String CHANNEL = "aeron:ipc";
    public static final int STREAM_ID = 1;

    public static void main(String[] args) throws Exception {
        MediaDriver driver = MediaDriver.launchEmbedded();
        Aeron.Context ctx = new Aeron.Context().aeronDirectoryName(driver.aeronDirectoryName());
        Aeron aeron = Aeron.connect(ctx);

        Publication pub = aeron.addPublication(CHANNEL, STREAM_ID);
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(256));

        String order = "BUY|AAPL|100|150.00";
        buffer.putStringWithoutLengthUtf8(0, order);
        long result = pub.offer(buffer, 0, buffer.capacity());
        System.out.println("Order sent: " + order + " (result: " + result + ")");

        Thread.sleep(1000);
        aeron.close();
        driver.close();
    }
}
