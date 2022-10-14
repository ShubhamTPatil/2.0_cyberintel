package com.marimba.apps.securitymgr.utils;

import java.io.IOException;
import java.io.InputStream;

import com.marimba.castanet.http.HTTPManager;
import com.marimba.tools.net.HTTPConfig;

public class ElasticUpCheck extends AbstractGetRequest {
	private static final String DEFAULT_ELASTIC_URL = "http://localhost:9200/marimba/securitymgmt";
	private final String elasticURL;
	
	public ElasticUpCheck(HTTPManager httpMgr, HTTPConfig httpConfig, String elasticURL) {
		super(httpMgr, httpConfig);
		this.elasticURL = elasticURL != null ? elasticURL : DEFAULT_ELASTIC_URL;
	}

	@Override
	protected void debug(int level, String msg) {
		if(DEBUG >= level) {
			System.out.println("ELASTICUPCHECK: "+msg);
		}
	}

	@Override
	protected String getConnectionURL() {
		return elasticURL;
	}

	@Override
	protected void readResponse(InputStream in, int contentLength) throws IOException {
		while(in.available() > 0) {
			in.read();
		}
	}

}
