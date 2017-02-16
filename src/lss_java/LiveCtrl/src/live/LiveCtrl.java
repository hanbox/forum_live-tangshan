package live;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import android.util.Log;

import com.baidubce.BceClientConfiguration;
import com.baidubce.BceServiceException;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.lss.LssClient;
import com.baidubce.services.lss.model.CreateSessionResponse;
import com.baidubce.services.lss.model.ListPresetsResponse;
import com.baidubce.services.lss.model.LivePreset;

public class LiveCtrl {
	// 设置AK/SK
	public String ACCESS_KEY_ID = "6932006e0b11421082cdc4a03293c1f1";
	public String SECRET_ACCESS_KEY = "9aa996c57a3047f0bb4cdbc7abe6c807";
	protected 	BceClientConfiguration m_config = null;
	public ThreadRecv m_thread = null;
	
	public LiveCtrl(){
		// 初始化一个LssClient
		m_config = new BceClientConfiguration();
		m_config.setCredentials(new DefaultBceCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY));
	}
	
	public void startServer(ServerSocket serviceSocket){
		m_thread = new ThreadRecv(serviceSocket);
		m_thread.start();
	}
	
	public class ThreadRecv extends Thread{
		boolean bState = true;
		ServerSocket m_serviceSocket = null;
		public ThreadRecv(ServerSocket serviceSocket){
			m_serviceSocket = serviceSocket;
		}
		
		public void run(){
			while ( bState ){
				try {
					if ( m_serviceSocket == null || m_serviceSocket.isClosed() ){
						continue;
					}
					
					Socket socket = m_serviceSocket.accept();
					
					InputStream inputStream =null;
					OutputStream outputStream =null;
					
					inputStream = socket.getInputStream();
		            byte buf[] = new byte[1024];
		            int len = 0;
		            len = inputStream.read(buf);
		            if ( len > 10 ){
		            	String strRecvData = new String(buf,0,len);
		            	String []list = strRecvData.split("/");
		            	if ( list.length > 1 ){
		            		if ( list[0].equals("createroom") ){
				            	if ( list.length >= 5 ){
					            	String description = list[1];
					            	String preset = list[2];
					            	String notification = list[3];
					            	String securityPolicy = list[4];
					            	String recording = list[5];
					            	st_session_info info = createSession(description, preset, notification, securityPolicy, recording);
					            	System.out.println(info);  
					            	
					            	String strSendData = "";
					            	strSendData += info.strPushUrl;
					            	strSendData += "/*****/";
					            	strSendData += info.strPullRtmpUrl;
					            	strSendData += "/*****/";
					            	strSendData += info.strSeeion_id;
					            	
						            //向客户端生成响应
						            outputStream = socket.getOutputStream();
						            outputStream.write(strSendData.getBytes());
						            outputStream.close();
				            	}
		            		}else if ( list[0].equals("delroom") ){
		            			String strSeeion_id = list[1];
		            			deleteSession(strSeeion_id);
		            		}
		            	}
		            }
					
					inputStream.close();
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		LiveCtrl ctrl = new LiveCtrl();
		ServerSocket server = new ServerSocket(8001);
		ctrl.startServer(server);		
	}
	
	public st_session_info createSession(String description, String preset, String notification,String securityPolicy, String recording){		
		LssClient client = new LssClient(m_config);
		st_session_info info = createPushSession(client, description, preset, null, null, null);
		
		return info;
	}
	
	public void deleteSession(String sessionId) {
		LssClient client = new LssClient(m_config);
		try{
			if ( client.getSession(sessionId) != null ){
				client.resumeSession(sessionId);
				client.deleteSession(sessionId);
			}	
		}catch(BceServiceException e){
			e.getMessage();
		}
	}
	
	public class st_session_info{
		String strSeeion_id;
		String strPushUrl;
		String strPullRtmpUrl;
	}
	
	private st_session_info createPushSession(LssClient client, String description, String preset, String notification,String securityPolicy, String recording) {
	    CreateSessionResponse resp = client.createSession(description, preset, notification, securityPolicy, recording, null);

	    st_session_info info = new st_session_info();
	    info.strPushUrl = resp.getPublish().getPushUrl();
	    info.strSeeion_id = resp.getSessionId();
	    info.strPullRtmpUrl = resp.getPlay().getRtmpUrl();
	    
	    return info;
	}
	
	private List<LivePreset> listPresets(LssClient client) {
	    ListPresetsResponse resp = client.listPresets();
	    
	    return resp.getPresets();
	}
	
	private void deleteSession(LssClient client, String sessionId) {
		try{
			if ( client.getSession(sessionId) != null ){
				client.resumeSession(sessionId);
				client.deleteSession(sessionId);
			}	
		}catch(BceServiceException e){
			e.getMessage();
		}
	}

}
