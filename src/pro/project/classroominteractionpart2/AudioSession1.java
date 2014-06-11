package pro.project.classroominteractionpart2;

import static android.media.MediaRecorder.AudioSource.VOICE_COMMUNICATION;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.regex.Pattern;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

public class AudioSession1 {
	static {
		System.loadLibrary("speex");
	}
	private native void Java_speex_EchoCanceller_open
	  (int jSampleRate, int jBufSize, int jTotalSize); 
	
	private native short[] Java_speex_EchoCanceller_process
	  (short[] input_frame, short[] echo_frame);
	
	private native void Java_speex_EchoCanceller_close();
	
	private boolean isRecording = false;
	public AudioRecord recorder;
	private int port = 50005;
	private int sampleRate = 44100;
	private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	private int encodingFormat = AudioFormat.ENCODING_PCM_16BIT;
	int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig,
			encodingFormat)+4096;
	int bufferSize = 0;
	public String ipAddress = "192.168.137.156";
	public static final String PERMISSION_TEXT = "You may start talking";
	DatagramSocket socket, socket1, socket2, socket3;
	
	public void stopStreaming() { // TO STOP AUDIO RECORDER BY RELEASING IT
		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
			Java_speex_EchoCanceller_close();

		}

	}//End StopStreaming
	
	
	/********************************START RECORDING*****************************************************/
	
	
	public void startStreaming() { // START AUDIO RECORDING
		  
		
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					
					short[] recordedShorts=new short[8192*2];
					short[] recvShorts=new short[8192*2];
					short[] filteredShorts;
					byte[] audioBytes;
					Java_speex_EchoCanceller_open(sampleRate, minBufSize, minBufSize);
					Log.e("minBufSize", minBufSize+"");		
					DatagramPacket packet;
					final InetAddress destination = InetAddress.getByName(ipAddress);
					 Log.e("before recorder", "about to initialize");
					recorder = new AudioRecord(VOICE_COMMUNICATION, sampleRate, channelConfig,
							encodingFormat, minBufSize * 10); // INITIALIZE
																// RECORDER
					
					if (recorder.getState() == AudioRecord.STATE_INITIALIZED) // CHECK
																				// IF
					{														// RECORDER
																				// INITIALIZED
						recorder.startRecording();
						bufferSize = recorder.read(recordedShorts, 0, recordedShorts.length);
					}
					else
						Log.e("not initialized", "kuch aur kar");

					while (isRecording) { // KEEP ON RECORDING IN PARALLEL
											// UNTILL STOP STREAMING IS CALLED
						recorder.read(recvShorts, 0, recvShorts.length);
						filteredShorts = Java_speex_EchoCanceller_process(recordedShorts, recvShorts);
						audioBytes = new byte[bufferSize*2];
						ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(filteredShorts);
						packet = new DatagramPacket(audioBytes, audioBytes.length,
								destination, port);
						socket.send(packet);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					socket.close();
				}
			}
		}).start();
	}//End StartStreaming
	
	
	public void onRequestPress() { // RAISE REQUEST FOR AUDIO DOUBT
		final byte[] request = ("Raise Hand").getBytes();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// port=getPort();
					final InetAddress destination = InetAddress
							.getByName(ipAddress);
					socket1 = new DatagramSocket();
					socket1.send(new DatagramPacket(request, request.length,
							destination, port));
					 Log.e("REquest", "Ssnt");
					 isRecording=true;
					 startStreaming();//while (waitingForPermission()); // SEND PERMISSION
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					socket1.close();
				}
			}
		}).start();
	}//End onRequestPress

	
	public void onWithdrawPress() { // IF WITHDRAW BUTTON PRESSED DURING  STREAMING
		
final byte[] request = ("Withdraw").getBytes();
new Thread(new Runnable() {
@Override
public void run() {
try {
final InetAddress destination = InetAddress
.getByName(ipAddress);
socket3 = new DatagramSocket();
socket3.send(new DatagramPacket(request, request.length,
destination, port)); // SEND WITHDRAW REQUEST TO
						// SERVER
isRecording = false;
stopStreaming(); // STOP STREAMING
} catch (SocketException e) {
e.printStackTrace();
} catch (UnknownHostException e) {
e.printStackTrace();
} catch (IOException e) {
e.printStackTrace();
} finally {
socket3.close();
}
}
}).start();
}//End onWithdrawPress
	
	
	
}//End AudioSession1

