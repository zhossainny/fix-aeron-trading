import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class AeronOrderReceiver {
    public static final String CHANNEL = "aeron:ipc";
    public static final int STREAM_ID = 1;

    public static void main(String[] args) throws Exception {
        // Start Aeron MediaDriver
        MediaDriver driver = MediaDriver.launchEmbedded();
        Aeron.Context ctx = new Aeron.Context().aeronDirectoryName(driver.aeronDirectoryName());
        Aeron aeron = Aeron.connect(ctx);

        // Create Aeron subscription to listen for messages
        Subscription sub = aeron.addSubscription(CHANNEL, STREAM_ID);
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(256));

        // Poll Aeron for new messages (e.g., ExecutionReport or other types)
        while (true) {
            sub.poll((buf, offset, length, header) -> {
                String raw = buf.getStringWithoutLengthUtf8(offset, length);
                System.out.println("Received Aeron message: " + raw);

                // Here you can process the message as needed.
                // For instance, if you receive an order, you can parse and act upon it.
                String[] parts = raw.split("\\|");
                if (parts.length == 4) {
                    String side = parts[0];
                    String symbol = parts[1];
                    String qty = parts[2];
                    String price = parts[3];

                    System.out.println("Side: " + side + ", Symbol: " + symbol + ", Quantity: " + qty + ", Price: " + price);
                } else {
                    System.out.println("Invalid message format.");
                }
            }, 10);
        }
    }
}
