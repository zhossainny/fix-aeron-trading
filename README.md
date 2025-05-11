# Aeron-FIX Integration Project

This project simulates a trading system using **Aeron IPC** and **QuickFIX/J**. It includes 4 components that simulate a basic order lifecycle from Aeron to a FIX engine and back.
You must start them in a logical order to avoid errors:

1. Start FixAcceptor – so that the initiator has something to connect to.
2. Start FixOrderInitiator – connects to acceptor, listens via Aeron.
3. Start AeronReceiver – listens for execution reports.
4. Start AeronOrderSender – triggers the entire flow.
   (Issue to resolve add vm option: "-add-opens java.base/sun.nio.ch=ALL-UNNAMED")
---

## 📦 Modules

### 1. **AeronOrderSender**
- Publishes a simulated trade order via Aeron IPC.
- Format: `BUY|AAPL|100|150.00`
- Sends to `FixOrderInitiator`.

### 2. **FixOrderInitiator**
- Listens via Aeron IPC for order messages.
- Parses and forwards the order as a FIX `NewOrderSingle` to the `FixAcceptor`.
- Uses QuickFIX/J as an Initiator.

### 3. **FixAcceptor**
- Acts as an Exchange mock.
- Receives orders from the initiator and replies with an `ExecutionReport`.
- Sends execution report via Aeron IPC to `AeronReceiver`.

### 4. **AeronReceiver**
- Listens on Aeron IPC for `ExecutionReport` messages.
- Prints/logs them.

---

## ▶️ How to Run

> Start components in this order to ensure proper IPC and FIX flow.

### 1. Start `FixAcceptor`
```bash
Run FixAcceptor.java

Summary of Flow:
Aeron sends raw message (BUY|AAPL|100|150.00) to AeronOrderReceiver via IPC.

AeronOrderReceiver maps the raw message to a FIX NewOrderSingle and sends it to FIX Initiator.

FIX Initiator sends the properly structured FIX message to the FIX Acceptor via TCP.

FIX Acceptor (exchange) processes the message and responds with an Execution Report.

FIX Initiator (receiver) receives the Execution Report and processes it accordingly.

AND
Message protocols
Aeron → FIX Initiator = IPC

FIX Initiator ↔ FIX Acceptor = TCP

FIX Acceptor → Aeron = IPC

Note: Aeron IPC (Inter-Process Communication) is a high-performance transport 
mode in the Aeron messaging system that allows processes on the same machine 
to communicate via shared memory, not via sockets.
