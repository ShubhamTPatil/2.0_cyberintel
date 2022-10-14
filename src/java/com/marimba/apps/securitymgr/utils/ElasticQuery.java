package com.marimba.apps.securitymgr.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.marimba.apps.securitymgr.utils.json.JSONObject;
import com.marimba.castanet.http.HTTPManager;
import com.marimba.intf.tuner.ILoginService;
import com.marimba.tools.net.HTTPConfig;

public class ElasticQuery extends AbstractPostRequest {
	private static final String DEFAULT_ELASTIC_URL = "http://localhost:9200/marimba/securitymgmt/_search";

	private final String elasticURL;
	private final JSONObject query;
	
	private String queryResponse;
	
	public ElasticQuery(HTTPManager httpMgr, HTTPConfig httpConfig, String elasticURL, JSONObject query) {
		super(httpMgr, httpConfig);
		this.elasticURL = elasticURL != null ? elasticURL : DEFAULT_ELASTIC_URL;
		this.query = query;
		this.queryResponse = null;
	}

	@Override
	protected void debug(int level, String msg) {
		if(DEBUG >= level) {
			System.out.println("ELASTICQUERY: "+msg);
		}
	}

	@Override
	protected String getConnectionURL() {
		return elasticURL;
	}

	@Override
	protected boolean writeRequest(OutputStream out) throws IOException {
		out.write(query.toString().getBytes());
		out.flush();
		return true;
	}

	@Override
	protected void readResponse(InputStream in, int contentLength) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		while(in.available() > 0) {
			bout.write(in.read());
		}
		byte[] response = bout.toByteArray();
		bout.close();
		queryResponse = new String(response);
	}
	
	public String getResponse() {
		return queryResponse;
	}

}
