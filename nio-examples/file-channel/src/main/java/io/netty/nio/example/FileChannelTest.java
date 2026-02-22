package io.netty.nio.example;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelTest {
    public static void main(String[] args) throws IOException {
        // 1. 获取 File（自动处理 JAR）
        File file = getResourceFile("data/nio-data.txt");
        
        RandomAccessFile aFile = new RandomAccessFile(file, "rw");
        FileChannel inChannel = aFile.getChannel();

        //create buffer with capacity of 48 bytes
        ByteBuffer buf = ByteBuffer.allocate(48);

        int bytesRead = inChannel.read(buf); //read into buffer.
        while (bytesRead != -1) {

            System.out.println("Read " + bytesRead);
            buf.flip();  //make buffer ready for read

            while(buf.hasRemaining()){
                System.out.print((char) buf.get());  // read 1 byte at a time
            }

            buf.clear();  //make buffer ready for writing
            bytesRead = inChannel.read(buf);
        }
        aFile.close();
    }
    
    
    
    /**
     * 把 classpath 下的资源文件转为本地 File（JAR 内会自动解压到临时文件）
     */
    public static File getResourceFile(String resourcePath) throws IOException {
        // resourcePath 示例："data.bin" 或 "folder/data.bin"（不带开头的 /）
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new FileNotFoundException("资源文件未找到: " + resourcePath);
        }
        
        String protocol = url.getProtocol();
        if ("file".equals(protocol)) {
            // IDE 或未打包时，直接返回文件
            try {
                return new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        } else if ("jar".equals(protocol)) {
            // JAR 包内：复制到临时文件
            File tempFile = File.createTempFile("resource-", "-" + resourcePath.replace("/", "_"));
            tempFile.deleteOnExit();   // 程序退出时自动删除
            
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
                 FileOutputStream fos = new FileOutputStream(tempFile)) {
                
                if (is == null) {
                    throw new FileNotFoundException("资源流为空: " + resourcePath);
                }
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }
            return tempFile;
        } else {
            throw new IOException("不支持的协议: " + protocol);
        }
    }
}
