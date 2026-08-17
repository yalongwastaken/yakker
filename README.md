# Yakker

A multi-channel chat application written in Java. Clients connect to a multithreaded server, join named channels, and exchange messages in real time through a Swing GUI.

Built as an OOP exercise exploring networked client-server architecture, multithreading, and GUI design.

> **Status: complete.**

## How It Works

The server manages a set of named channels. Each channel tracks its connected clients and broadcasts messages to everyone in it. Clients connect over TCP, pick a username and channel, and communicate through a Swing interface.

The networking layer runs on a background thread so the GUI stays responsive. Connecting, disconnecting, and sending messages are handled asynchronously — the UI never blocks waiting on the server.

## Repository Structure
yakker/
├── src/
│   └── main/java/yakker/
│       ├── YakChannel.java          — server-side channel, manages connected clients and broadcasts
│       ├── YakClientFrame.java      — Swing frame, wires together the GUI and action listeners
│       ├── YakClientGUI.java        — entry point, launches the client application
│       └── YakClientNetworking.java — handles TCP connection and background read thread
├── work/                            — compiled output
├── .gitignore
└── README.md

## Class Structure
YakClientGUI        — entry point, launches the client application
YakClientFrame      — Swing frame, wires together the GUI and action listeners
YakClientNetworking — handles TCP connection and background read thread
YakChannel          — server-side channel, manages connected clients and broadcasts

## Requirements

Install Java via Homebrew if you don't have it:

```bash
brew install openjdk
sudo ln -sfn /opt/homebrew/opt/openjdk/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk.jdk
echo 'export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

Verify:

```bash
java -version
javac -version
```

## Building and Running

### Compile

From the repo root:

```bash
javac -d work src/main/java/yakker/*.java
```

### Start the Server

```bash
java -cp work yakker.YakChannel <port>
```

### Start a Client

```bash
java -cp work yakker.YakClientGUI
```

Enter the server address, port, username, and channel name in the GUI to connect.

## Author

**Anthony Yalong**
- Email: yalong.anthony123@gmail.com
- GitHub: [@yalongwastaken](https://github.com/yalongwastaken)
