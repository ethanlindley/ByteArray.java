package com.Zaseth;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

class ByteArrayLogger {
	private int bytesAvailable = -1;
	private int position = -1;
	private byte[] stream;

	public ByteArrayLogger() {
	
	}

	public String stringToByteArray(int bytesAvailable, int position, byte[] stream) {
                StringBuilder sb = new StringBuilder();
                sb.append("Bytes available: " + bytesAvailable + "\r\n");
                sb.append("Position: " + this.position + "\r\n");
                sb.append("Byte stream: " + Arrays.toString(stream).substring(0, 120));
                return this.Debug(sb.toString());
	}
	
	public String getTime() {
	        return new SimpleDateFormat("yyyy-MM-dd-)HH:mm:ss").format(Calendar.getInstance().getTime());
	}
	
	public String Debug(String sb) {
	        String toPrint = "<DEBUG=";
	        toPrint += this.getTime();
	        toPrint += ">\r\n" + sb;
	        toPrint += "\r\n</DEBUG>";
	        return toPrint;
	}
	
	public static void main(String[] args) {
	        ByteArrayLogger test = new ByteArrayLogger();
	        System.out.println(test.stringToByteArray(1024, 0, new byte[1024]));
	}
}
