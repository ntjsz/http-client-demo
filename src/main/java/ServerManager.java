import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by hht on 2017/10/9.
 */
public class ServerManager {

    static ByteBuffer byteBuffer;
    static ByteBuffer response;
    static String re = "HTTP/1.1 200 OK\nContent-Length: 13\n\nhht-response\n";

    public static void main(String[] args) throws Exception{
        byteBuffer = ByteBuffer.allocateDirect(1024);
        response = ByteBuffer.allocate(1024);
        response.put(re.getBytes());

        Selector selector = Selector.open();
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(InetAddress.getLocalHost(), 1234));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            if(selector.select(100) > 0) {
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if(key.isAcceptable()) {
                        if(key.channel() instanceof ServerSocketChannel) {
                            SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            iterator.remove();
                        }
                    } else if (key.isReadable()) {
                        if(key.channel() instanceof SocketChannel) {
                            try{
                                handleRequest((SocketChannel) key.channel());
                            } catch (Exception e) {
                                System.err.println(e);
                                key.channel().close();
                                key.cancel();
                            }

                            iterator.remove();
                        }
                    }/* else if (key.isWritable()) {
                        if(key.channel() instanceof SocketChannel) {
                        }
                    }*/
                }
            }
        }
    }

    static void handleRequest(SocketChannel socketChannel) throws Exception{
        byteBuffer.clear();
        int count;
        while ((count = socketChannel.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            System.out.println(count);

            byte[] b = new byte[count];
            byteBuffer.get(b);
            System.out.println(new String(b));
            byteBuffer.clear();
            response.flip();
            socketChannel.write(response);
        }
    }
}
