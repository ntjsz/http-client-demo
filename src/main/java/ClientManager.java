import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by hht on 2017/10/10.
 */
public class ClientManager {

    private static int size = 1024;
    private static ByteBuffer byteBuffer;
    private static SocketChannel socketChannel;


    public static void main(String[] args) throws Exception{
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(InetAddress.getLocalHost(), 1234));
        byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);


        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        while (true) {
            if (selector.select(100) > 0) {
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        if (key.channel() instanceof SocketChannel) {
                            int count = socketChannel.read(byteBuffer);
                            System.out.println(count);
                            byteBuffer.flip();
                            while (byteBuffer.hasRemaining()) {
                                System.out.print((char) byteBuffer.get());
                            }
                            byteBuffer.clear();
                            iterator.remove();
                        }
                    } else if(key.isWritable()) {
                        byteBuffer.clear();
                        byteBuffer.put("hi".getBytes());
                        byteBuffer.flip();
                        socketChannel.write(byteBuffer);
                        iterator.remove();
                    }
                }
            }
        }
    }
}
