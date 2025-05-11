import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.UnsafeBuffer;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.NewOrderSingle;

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

        // Initialize FIX session and initiator
        SessionSettings settings = new SessionSettings("config/initiator.cfg");
        FixApp app = new FixApp();
        MessageStoreFactory storeFactory = new MemoryStoreFactory();
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        Initiator initiator = new SocketInitiator(app, storeFactory, settings, logFactory, new DefaultMessageFactory());
        initiator.start();

        // Poll for incoming Aeron messages and send to FIX engine
        while (true) {
            sub.poll((buf, offset, length, header) -> {
                String raw = buf.getStringWithoutLengthUtf8(offset, length);
                String[] parts = raw.split("\\|");

                if (parts.length == 4) {
                    String side = parts[0];
                    String symbol = parts[1];
                    String qty = parts[2];
                    String price = parts[3];

                    // Create FIX message (NewOrderSingle)
                    NewOrderSingle order = new NewOrderSingle(
                            new ClOrdID("ORD" + System.currentTimeMillis()),
                            new Side(side.equals("BUY") ? Side.BUY : Side.SELL),
                            new TransactTime(),
                            new OrdType(OrdType.LIMIT)
                    );
                    order.set(new Symbol(symbol));
                    order.set(new OrderQty(Double.parseDouble(qty)));
                    order.set(new Price(Double.parseDouble(price)));

                    // Send the message to the FIX engine
                    try {
                        Session.sendToTarget(order, app.getSessionID());
                        System.out.println("Sent FIX message: " + order);
                    } catch (SessionNotFound e) {
                        System.err.println("Session not found: " + e.getMessage());
                    }
                } else {
                    System.out.println("Invalid message format.");
                }
            }, 10);
        }
    }

    static class FixApp extends MessageCracker implements Application {
        private SessionID sessionID;

        public SessionID getSessionID() {
            return sessionID;
        }

        public void onCreate(SessionID sessionID) {
            this.sessionID = sessionID;
        }

        public void onLogon(SessionID sessionID) {
            System.out.println("Logon: " + sessionID);
        }

        public void onLogout(SessionID sessionID) {
            System.out.println("Logout: " + sessionID);
        }

        public void toAdmin(Message message, SessionID sessionID) {}
        public void fromAdmin(Message message, SessionID sessionID) {}
        public void toApp(Message message, SessionID sessionID) throws DoNotSend {}
        public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
            crack(message, sessionID);
        }
    }
}
