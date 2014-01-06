package com.lollotek.umessage.classes;

import org.json.JSONObject;

public class HttpResponseUmsg {

	public boolean error;
	public JSONObject result;

	public HttpResponseUmsg() {
		this.error = false;
		this.result = null;
	}
}
