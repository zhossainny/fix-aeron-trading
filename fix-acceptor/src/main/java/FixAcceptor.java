import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;

public class FixAcceptor implements Application {
    private SessionID sessionID;

    public static void main(String[] args) throws Exception {
        SessionSettings settings = new SessionSettings("config/acceptor.cfg");
        FixAcceptor app = new FixAcceptor();
        MessageStoreFactory storeFactory = new MemoryStoreFactory();
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        Acceptor acceptor = new SocketAcceptor(app, storeFactory, settings, logFactory, new DefaultMessageFactory());
        acceptor.start();
    }

    @Override
    public void onCreate(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public void onLogon(SessionID sessionID) {
        System.out.println("Logon: " + sessionID);
    }

    @Override
    public void onLogout(SessionID sessionID) {
        System.out.println("Logout: " + sessionID);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {}

    @Override
    public void fromAdmin(Message message, SessionID sessionID) {}

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {}

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        if (message instanceof NewOrderSingle) {
            NewOrderSingle order = (NewOrderSingle) message;
            String orderId = order.getClOrdID().getValue();
            System.out.println("Received Order: " + orderId);

            // Simulate ExecutionReport
            ExecutionReport execReport = new ExecutionReport();

// Set the necessary fields
            execReport.set(new ClOrdID(order.getClOrdID().getValue())); // Use the ClOrdID from the order
            execReport.set(new ExecID("EX" + System.currentTimeMillis())); // Unique ExecID for the execution report
            execReport.set(new ExecType(ExecType.NEW)); // ExecType indicating the status of the order
            execReport.set(new OrdStatus(OrdStatus.NEW)); // OrdStatus indicating the order status
            execReport.set(new Symbol("AAPL")); // Example Symbol
            execReport.set(new LastQty(100));       // FIX 4.4: LastQty replaces LastShares
            execReport.set(new LastPx(150.00)); // Example price
            execReport.set(new LeavesQty(100)); // Remaining quantity (can be adjusted as needed)
            execReport.set(new CumQty(0)); // Cumulative quantity (adjusted based on executed quantity)

            try {
                Session.sendToTarget(execReport, sessionID);
            } catch (SessionNotFound e) {
                e.printStackTrace();
            }
        }
    }
}
