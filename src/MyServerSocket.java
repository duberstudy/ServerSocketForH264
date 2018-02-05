import java.io.*;
import java.net.*;
/**
 * ʵ�ַ�������
 * ���ڽ����ϴ������ݺ͹��ͻ�����������
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
        System.out.println("����������...");
        while(true){
            try {
                //ͨ��ServerSocket��accept������������,����ȡ�ͻ��˵�Socket����
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
     * ʵ�ַ������˵����ݴ���
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
            DataInputStream in= null; //��ȡSocket��������
            DataOutputStream dos = null; //д�ļ��������
            FileOutputStream fos = null;
            try {
                //����Scoket�����getInputStream����ȡ�ÿͻ��˷��͹�����������
                in = new DataInputStream(new BufferedInputStream(client.getInputStream()));

                String fileName = "testserver.h264"; //��ȡ�ļ���

                if(filePath.endsWith("/")==false&&filePath.endsWith("\\")==false){
                    filePath+="\\";
                }
                filePath += fileName;
                File file = new File(filePath);
                if(!file.exists()){
                    file.getParentFile().mkdirs();          
                }
                file.createNewFile();
                //�ӿͻ����ϴ���������
                //��ʼ�����ļ�
                fos = new FileOutputStream(file, true);
                dos = new DataOutputStream(new BufferedOutputStream(fos));
                int bufferSize = 1024;
                byte[] buf = new byte[bufferSize];

            	int num =0;
                while((num=in.read(buf))!=-1){
                	System.out.println(num);
                	String clientCommand = "";
                	clientCommand = new String(buf, "UTF-8");
                	if (clientCommand.indexOf("%7C")!=-1)//��Android�ͻ��˴��ݲ�������
                        clientCommand = clientCommand.replace("%7C", "|");//�滻UTF���ַ�%7CΪ|
                	String[] messages = clientCommand.split("\\|");
                	String tempStr = messages[0];//��һ���ַ���Ϊ����
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
                System.out.println("���ݽ�����ϣ�");
                
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
        //���÷������˿�
        int port = 8888;
        //���÷������ļ����λ��
        String dirPath = "G:\\FTPService\\";
        new MyServerSocket(ip,port,dirPath).run();
    }
}