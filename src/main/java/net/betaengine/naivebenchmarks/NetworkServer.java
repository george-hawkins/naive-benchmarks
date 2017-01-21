package net.betaengine.naivebenchmarks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkServer {
    private final Logger logger = LoggerFactory.getLogger(NetworkServer.class);
    
    private final int readPort;
    private final int writePort;
    
    public static void main(String[] args) {
        // The Benchmarks client READ_PORT is the server's write port and WRITE_PORT is its read port.
        NetworkServer server = new NetworkServer(Benchmarks.WRITE_PORT, Benchmarks.READ_PORT);
        
        server.run();
    }
    
    private NetworkServer(int readPort, int writePort) {
        this.readPort = readPort;
        this.writePort = writePort;
    }

    private void run() {
        try {
            Selector selector = Selector.open();
            
            createServerChannel(readPort, new ReaderOperation(), selector);
            createServerChannel(writePort, new WriterOperation(), selector);
            
            // Remember reading and writing are reversed from the client's perspective.
            logger.info("ready for client to write to {} and read from {}", readPort, writePort);
            
            while (true) {
                if (selector.select() > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    
                    for (SelectionKey key : keys) {
                        if (key.isAcceptable()) {
                            ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
                            SocketChannel channel = serverChannel.accept();
                            Operation operation = (Operation)key.attachment();
                            
                            operation.consume(channel.socket());
                            channel.close();
                        }
                    }
                    
                    keys.clear();
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void createServerChannel(int port, Operation operation, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();

        serverChannel.configureBlocking(false);
        serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        serverChannel.socket().bind(new InetSocketAddress(port));
        
        SelectionKey acceptKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        acceptKey.attach(operation);
    }
    
    private interface Operation {
        void consume(Socket socket) throws IOException;
    }
    
    private class ReaderOperation implements Operation {
        @Override
        public void consume(Socket socket) throws IOException {
            try (BufferedInputStream input = new BufferedInputStream(socket.getInputStream())) {
                long count = 0;
                
                while (input.read() != -1) {
                    count++;
                }
                
                logger.info("read {} bytes", count);
            }
        }
    }
    
    private class WriterOperation implements Operation {
        @Override
        public void consume(Socket socket) throws IOException {
            BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
            long count = 0;
            
            try {
                while (true) {
                    output.write(0);
                    count++;
                }
            } catch (IOException e) {
                // An exception is expected - this side reads forever and expects the client to close
                // the socket on its end resulting in a connection reset on this end.
                logger.info("wrote {} bytes", count);
            }
        }
    }
}