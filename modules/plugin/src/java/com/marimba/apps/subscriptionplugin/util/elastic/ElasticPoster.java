// Copyright 2017-2019, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin.util.elastic;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;

import com.marimba.apps.subscriptionplugin.util.json.JSONObject;
import com.marimba.apps.subscriptionplugin.*;

public class ElasticPoster implements IPluginDebug {
	private static final String DEFAULT_ELASTIC_URL = "http://localhost:9200/marimba/securitymgmt";

	private final ExecutorService threadedTasks;
	private final Object lock = new Object();
	private boolean stopped = false;

	private String elasticURL = null;

	class Record {
		private final String id;
		private final JSONObject obj;
		public Record(String id, JSONObject obj) {
			this.id = id;
			this.obj = obj;
	    }
		public void write(String index, String type, BufferedWriter writer) throws IOException {
			JSONObject outer = new JSONObject();
			JSONObject inner = new JSONObject(); outer.put("index", inner);
			inner.put("_index", index);
			inner.put("_type", type);
			inner.put("_id", this.id);
			writer.write(outer.toString()); writer.newLine();
			writer.write(obj.toString()); writer.newLine();
			writer.flush();
		}
	}
	
	private final List<Record> records;
	
	class InsertThread implements Runnable {
		private final String index;
		private final String type;
		
		public InsertThread(String index, String type) {
			this.index = index;
			this.type = type;
		}

		@Override
		public void run() {
			do {
				synchronized(lock) {
					if(records.size() > 0) {
						StringWriter strWriter = new StringWriter();
						BufferedWriter writer = new BufferedWriter(strWriter);
						for(Record record : records) {
							try {
								record.write(index, type, writer);
							} catch(Throwable t) {
								//do nothing
							}
						}
						records.clear();
						try {
							/* do the post */
							doPost(strWriter.toString());
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
				try {
					Thread.sleep(10000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while(!stopped);
		}
	}
	
	
	public ElasticPoster() {
		this.threadedTasks = Executors.newFixedThreadPool(1);
		this.records = new ArrayList<Record>();
	}

	public String getElasticURL() {
		return elasticURL;
	}

	public void setElasticURL(String elasticURL) {
		this.elasticURL = elasticURL;
	}

	public void start() {
		this.stopped = false;
		String url = DEFAULT_ELASTIC_URL;
		if (elasticURL != null) {
			url = elasticURL;
		}
		URL _obj;
		try {
			_obj = new URL(url);
			String filePortion = _obj.getFile();
			String[] portions = filePortion.split("/");
			String index = portions[1];
			String type = portions[2];
			this.threadedTasks.submit(new InsertThread(index, type));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		stopped = true;
		threadedTasks.shutdown();
	}
	
	public boolean submit(JSONObject singleRecord) {
		Record record = new Record(Integer.toString(singleRecord.hashCode()), singleRecord);
		synchronized(lock) {
			records.add(record);
		}
		return true;
	}

	private boolean doPost(String records) throws IOException {
		boolean retVal = false;
		String url = DEFAULT_ELASTIC_URL;
		if (elasticURL != null) {
			url = elasticURL;
		}

		URL _obj = new URL(url);
		URL obj = new URL(_obj.getProtocol()+"://"+_obj.getHost()+":"+_obj.getPort()+"/_bulk");
		
		debug(INFO, "Going to post() with FINAL url: "+obj.toExternalForm());
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Security Management Inserter");
		con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

		// For POST only - START
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		byte[] ptext = records.getBytes("UTF-8");
		os.write(ptext);
		os.flush();
		os.close();

		// For POST only - END
		int responseCode = con.getResponseCode();
		debug(INFO, "doPost() with specific content type, Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			debug(INFO, "doPost(), response.toString() - " + response.toString());
			retVal = true;
		} else {
			debug(ERROR, "doPost(), request not worked");
		}
		return retVal;
	}

	private void debug(boolean debugType, String msg) {
		if (debugType) {
			StringBuffer messageBuf = new StringBuffer();
			messageBuf.append("Security Plugin => ");
			messageBuf.append(new Date());
			messageBuf.append(" - ");
			messageBuf.append("ElasticPoster.java -- " + msg);
			System.out.println(messageBuf);
			messageBuf = null;
		}
	}

}

