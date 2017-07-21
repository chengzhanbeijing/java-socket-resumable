package fileupload.socket.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Part;

public class SocketUtils implements Runnable{

	private final static Logger LOGGER =Logger.getLogger(SocketUploadClient.class.getCanonicalName());
	private static final long serialVersionUID = 3280911953957165069L;
	private String serverAddress = "127.0.0.1"; // for testing set to localhost
	private int serverPort = 8001;
	private String filePath = null;
	private Socket socket = null;
	private byte[] boundary = null;
	private String basePath = null;
	private Part filePart = null;
	private boolean flag = true;
	public static byte SEMI_COLON = 59;
	public static byte CR = 13;
	public static byte LF = 10;
	public static byte COLON = 58;
	public static byte[] END_HEADER = new byte[]{SEMI_COLON, CR, LF, CR, LF};
	public static CountDownLatch countDownLatch = null;
	public static String OK = "ok";
	public static String TYPE_FILE = "Type : File";
	public static String TYPE_DIRECTORY = "Type : Directory";
	
	private static int BUFFER_SIZE = 1024 * 64;
	private static long start = 0l;
	
	
	
	Random rnd = new Random();
	
	private static int BOUNDARY_LENGTH = 24;
	
	private int errorCount = 0;
	
	private long respTime = 0l;
	 public void execute(String path,InputStream ins) throws Exception {
	    	/*String address = readServerAddress();
	    	if(address != null && address.trim().length() != 0) {
	    		serverAddress = address; 
	    	}*/
	    	
	    	long startTime;
	    	long endTime;
	    	
	    	/*int port = readServerPort();
	    	if(port != -1) {
	    		serverPort = port;
	    	}*/
	    	File f = null;
	    	while(true) {
	    		//filePath = readFilePath();
	    		filePath = "c:"+File.separator+path;
	    		/*if(filePath == null || "".equals(filePath.trim())) {
	    			System.out.println("Please enter valid file name.");
	    			continue;
	    		}*/
	    		f = new File(filePath);
	    		/*if(!f.exists()) {
	    			System.out.println("File does not exist " + filePath + ". Please recheck filename.");
	    			continue;
	    		}*/
	    		break;
	    	}
	    	if(f.isDirectory()) {
	    		String recursive = readString("You entered a directory path. " +
	    				"Do you want to upload all files recursively from that directory?(Y/n) : ");
	    		if(recursive == null || "".equals(recursive)) {
	    			recursive = "y";
	    		}
	    		if("y".equalsIgnoreCase(recursive)) {
	    			startTime = System.currentTimeMillis();
	    			start = startTime;
	    			//recursiveSendFile();
	    		} else {
	    			return;
	    		}
	    	} else {
	    		startTime = System.currentTimeMillis();
	    		start = startTime;
	    		sendFile(ins);
	    	}
	    	endTime = System.currentTimeMillis();
	    	closeSocket(socket);
	    	System.out.println("Total time to send files " + (endTime - startTime) / 1000 + "s");
	    	System.out.println("Time for processing response " + (respTime/1000) + "s");
	    	System.out.println("Total errors : " + errorCount);
	    	
	    }
	 public void closeSocket(Socket skt) throws Exception {
	        skt.close();
	    }
	public boolean aa(Part filePart){
		boolean flag = true;
		try {
    		final String fileName = getFileName(filePart);
    		System.out.println(fileName);
    		execute(fileName,filePart.getInputStream());
    		countDownLatch.countDown();
    	}catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}
	public  void sendFile(InputStream ins) throws Exception {
	    	
	    	File f = new File(filePath);
	    	
	    	/*if(!f.exists()) {
	    		sop("File does not exist " + filePath + ". Please recheck filename.");
	    		return;
	    	}*/
	    	if(socket == null) {
	    		socket = connect(serverAddress, serverPort);
	    	}
	    	sendSingle(f,ins);
	    	//closeSocket(socket);
	 }
	protected String readString(String message) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter writer = new PrintWriter(new PrintWriter(System.out),true);
		String input = null;
		
		try {
			writer.printf(message);
			input = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
		
		return input;
	}
	private String getFileName(final Part part) {
		final String partHeader = part.getHeader("content-disposition");
	    LOGGER.log(Level.INFO, "Part Header = {0}", partHeader);
	    for (String content : part.getHeader("content-disposition").split(";")) {
	        if (content.trim().startsWith("filename")) {
	            return content.substring(
	                    content.indexOf('=') + 1).trim().replace("\"", "");
	        }
	    }
	    return null;
	}
	 public Socket connect(String server,int port) throws Exception {
	        
	        //Socket skt = new Socket(server, port);
	    	Socket skt = new Socket();
	        //skt.setSendBufferSize(BUFFER_SIZE);
	        //sop("SO_SNDBUF " + skt.getSendBufferSize());
	        skt.setTcpNoDelay(true);
	    	SocketAddress address = new InetSocketAddress(server, port);
	        skt.connect(address);
	        return skt;
	    }
	 public void sendSingle(File f,InputStream ins) throws Exception {
	    	
	    	sendFilename(socket, f);
	    	// check response to see if file already exists.
	    	if(checkFileExistsInServer(socket)) {
	    		return;
	    	}
	    	if(f.isDirectory()) {
	    		String resp = processResponse(socket);
	    		sop(resp);
	    		//closeSocket(socket);
	    		return;
	    	}
	    	
	    	boundary = generateBoundary();
	    	sendBoundary(socket,boundary);
	    	sendFile(socket, f, boundary,ins);
	    	String resp = processResponse(socket);
	    	sop(resp);
	    	
	    }
	 public void sendFilename(Socket skt, File file) throws Exception {
	    	String filename = file.getName();
	        OutputStream os = skt.getOutputStream();
	        BufferedOutputStream bos = new BufferedOutputStream(os);
	        //if(file.isFile()) {
	        	bos.write(TYPE_FILE.getBytes());
	        //} else {
	        	//bos.write(TYPE_DIRECTORY.getBytes());
	        //}
	        bos.write(END_HEADER);
	        if(basePath != null) {
	        	String apath = file.getAbsolutePath();
	        	String p = apath.substring(apath.indexOf(basePath) + basePath.length() + 1);
	        	if(!p.equals(filename)) {
		        	//sop("Sending base path " + p);
		        	filename = p;
	        	}
	        }
	        bos.write(filename.getBytes());
	        bos.write(END_HEADER);
	        bos.flush();
	        //System.out.println("Sent file name " + filename);
	    }
	 public boolean checkFileExistsInServer(Socket socket) throws Exception {
	    	boolean fileexists = false;
	    	// check response to see if file already exists.
	        String resp = processResponse(socket);
	        if(!OK.equals(resp)) {
	        	sop("Error sending file - " + resp);
	        	System.exit(1);
	        	fileexists = true;
	        }
	    	return fileexists;
	    }
	 
	 public String processResponse(Socket skt) throws Exception{
	    	long start = System.currentTimeMillis();
	    	boolean foundEndHeader = false;
	    	//System.out.println("Waiting for response");
	    	
	    	StringBuilder sb = new StringBuilder();
	    	
	        InputStream bis = skt.getInputStream();
	        //byte[] buff = new byte[1];
	        int c = -1;
	        // responses will be small so read that to memory.
	        try{
		        while((c = bis.read()) != -1) {
		        	//sb.append(new String(Arrays.copyOf(buff, c)));
		        	sb.append((char)c);
		        	System.out.println((char)c);
		        	if(sb.toString().endsWith(new String(END_HEADER))) {
		        		//sop("Found header " + sb);
		        		// Strip END_HEADER from response.
		        		sb.replace(sb.length() -5, sb.length(), "");
		        		foundEndHeader = true;
		        		break;
		        	}
		        }
		     }catch(Exception e){
		    	 e.printStackTrace();
		     }
	        if(!foundEndHeader) {
	        	sop("1 header not found in reponse " + sb);
	        	if(sb.toString().endsWith(new String(END_HEADER))) {
	        		// Strip END_HEADER from response.
	        		sb.replace(sb.length() -5, sb.length(), "");
	        		foundEndHeader = true;
	        		System.exit(1);
	        	}
	        	
	        }
	        if(!foundEndHeader) {
	        	sop("2 header not found in reponse " + sb);
	        	System.exit(1);
	        }
	        respTime += System.currentTimeMillis() - start; 
	        return sb.toString();
	    }
	 public static void sop(String m) {
			System.out.println(m);
		}
	 protected byte[] generateBoundary() {
	    	
	    	byte[] ia = new byte[BOUNDARY_LENGTH];
			for(int i=0; i<ia.length; i++) {
				int a = rnd.nextInt(9);
				ia[i] = (byte)a;
			}
			//sop("boundary - ");
			//for(int i=0; i<ia.length; i++) {
			//	System.out.print(ia[i]);
			//}
			//System.out.println();
			
			return ia;
	    }
	 public void sendBoundary(Socket skt, byte[] bnd) throws Exception {
	    	OutputStream os = skt.getOutputStream();
	        BufferedOutputStream bos = new BufferedOutputStream(os);
	        bos.write(bnd);
	        //bos.write(new String("123456789012345678901234").getBytes());
	        bos.write(END_HEADER);
	        bos.flush();
	        //System.out.println("Sent boundary.");
	    }
	 public String formatSize(long size){
    	 if (size < 1024) {
	        return size + " bytes";
	      } else if (size < 1024 * 1024) {
	        return Math.round((size / 1024.0)) + " KB";
	      } else if (size < 1024 * 1024 * 1024) {
	        return String.format("%.1f",size / 1024.0 / 1024.0) + " MB";
	      } else {
	        return String.format("%.1f",size / 1024.0 / 1024.0 / 1024.0) + " GB";
	      }
    }
	 public void sendFile(Socket skt, File f, byte[] bnd,InputStream ins)  throws IOException {
	    	
	    	try {
		        OutputStream os = skt.getOutputStream();
		        BufferedOutputStream bos = new BufferedOutputStream(os, BUFFER_SIZE);
		        BufferedInputStream bis = new BufferedInputStream(ins, BUFFER_SIZE);
		        int c = 0;
		        byte[] buff = new byte[BUFFER_SIZE];
		        long readed = 0l;
		        //long size = f.length();
		        long size = ins.available();
		        double m = Math.round(readed*100.0/size);
		        while((c = bis.read(buff)) != -1 ) {
		            bos.write(buff,0,c);
		            bos.flush();
		            readed +=c;
		            m = readed*100.0/size;
		            long time = (System.currentTimeMillis()-start)/1000;
		            long speed = 0l;
		            if(time!=0){
		            	speed = readed/time;
		            }
		            System.out.println(Thread.currentThread().getName()+",上传进度:"+m+"%"+",文件大小："+size+",已上传大小:"+readed+",已上传："+formatSize(readed)+",已用时:"+time+"s"+",上传速度:"+formatSize(speed)+"/s");
		        }
		        // Write boundary to end sending file.
		        bos.write(bnd);
		        //bos.write(new String("123456789012345678901234").getBytes());
		        bos.flush();
		        bis.close();
		        sop("Completed sending file " + f.getName());
	    	} catch(IOException ioe) {
	    		errorCount++;
	    		sop("Error sending file " + f.getName());
	    		ioe.printStackTrace();
	    		throw ioe;
	    	}
	    }

	@Override
	public void run() {
		if(filePart!=null){
    		this.flag = aa(filePart);
    	}
		System.out.println(this.flag);
		
	}
	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public Part getFilePart() {
		return filePart;
	}

	public void setFilePart(Part filePart) {
		this.filePart = filePart;
	}

}
