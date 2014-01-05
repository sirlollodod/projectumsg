package com.lollotek.umessage.classes;

import org.json.JSONObject;

public class HttpResponse {

	public boolean error;
	public JSONObject result;

	public HttpResponse() {
		this.error = false;
		this.result = null;
	}
}
