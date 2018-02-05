import java.io.*;
import java.net.*;
/**
 * 实现服务器端
 * 用于接收上传的数据和供客户端下载数据
 * @author DELL
 *
 */
public class MyServerSocket {
    private int port;
    private String ip;
    private String host;
    private String dirPath;
    private static ServerSocket server;
    
    private final int videoFlag = 19;
    private final int connectFlag = 18;
    private final int disconnectFlag = 21;
    
    
    public MyServerSocket(String ip, int port,String dirPath){
    	this.ip = ip;
        this.port = port;
        this.dirPath = dirPath;
        this.server = null;
    }
    
    public void run(){
        if(server==null){
            try {
                server = new ServerSocket();
                server.bind(new InetSocketAddress(ip, port));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("服务已启动...");
        while(true){
            try {
                //通过ServerSocket的accept方法建立连接,并获取客户端的Socket对象
                Socket client = server.accept();
                if(client==null) continue;
                System.out.println("Connected...");
                new SocketConnection(client,this.dirPath).run();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    /**
     * 实现服务器端的数据传输
     * @author DELL
     *
     */
    public class SocketConnection extends Thread{
        private Socket client;
        private String filePath;
        
        public SocketConnection(Socket client, String filePath){
            this.client = client;
            this.filePath = filePath;
            
        }
        
        public void run(){
            if(client==null) return;
            DataInputStream in= null; //读取Socket的输入流
            DataOutputStream dos = null; //写文件的输出流
            FileOutputStream fos = null;
            try {
                //访问Scoket对象的getInputStream方法取得客户端发送过来的数据流
                in = new DataInputStream(new BufferedInputStream(client.getInputStream()));

                String fileName = "testserver.h264"; //获取文件名

                if(filePath.endsWith("/")==false&&filePath.endsWith("\\")==false){
                    filePath+="\\";
                }
                filePath += fileName;
                File file = new File(filePath);
                if(!file.exists()){
                    file.getParentFile().mkdirs();          
                }
                file.createNewFile();
                //从客户端上传到服务器
                //开始接收文件
                fos = new FileOutputStream(file, true);
                dos = new DataOutputStream(new BufferedOutputStream(fos));
                int bufferSize = 1024;
                byte[] buf = new byte[bufferSize];

            	int num =0;
                while((num=in.read(buf))!=-1){
                	System.out.println(num);
                	String clientCommand = "";
                	clientCommand = new String(buf, "UTF-8");
                	if (clientCommand.indexOf("%7C")!=-1)//从Android客户端传递部分数据
                        clientCommand = clientCommand.replace("%7C", "|");//替换UTF中字符%7C为|
                	String[] messages = clientCommand.split("\\|");
                	String tempStr = messages[0];//第一个字符串为命令
                	if (num==videoFlag&&tempStr.equals("PHONEVIDEO")){
                		System.out.println("PHONEVIDEO");
                	} else if (num==connectFlag&&tempStr.equals("PHONECONNECT")){
                		System.out.println("PHONECONNECT");
                	} else if (num==disconnectFlag&&tempStr.equals("PHONEDISCONNECT")){
                		System.out.println("PHONEDISCONNECT");
                	} else{
                		if (tempStr.equals("PHONEVIDEO")){
                			dos.write(buf, videoFlag, buf.length - videoFlag);
                		} else{
                			dos.write(buf, 0, num);
                        System.out.println("WRITE");
                		}
                	}
                }
                dos.flush();
                System.out.println("数据接收完毕！");
                
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                try {
                    if(in!=null)  in.close();
                    if(dos!=null) dos.close();
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public boolean isZero(byte[] buf, int start) {
    	for (int i = start; i < buf.length; i++) {
	    	if (buf[i] != 0) {
	    		return false;
	    	}
    	}
    	return true;
    }

    public static void main(String[] args){
    	String ip = "192.168.254.131";
//    	String ip = "192.168.191.1";
        //设置服务器端口
        int port = 8888;
        //设置服务器文件存放位置
        String dirPath = "G:\\FTPService\\";
        new MyServerSocket(ip,port,dirPath).run();
    }
}