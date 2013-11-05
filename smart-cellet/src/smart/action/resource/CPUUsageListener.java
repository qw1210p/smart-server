package smart.action.resource;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import net.cellcloud.common.Logger;
import net.cellcloud.core.Cellet;
import net.cellcloud.talk.dialect.ActionDialect;
import net.cellcloud.util.ObjectProperty;
import net.cellcloud.util.Properties;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.DeferredContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import smart.action.AbstractListener;
import smart.api.API;
import smart.api.RequestContentCapsule;
import smart.api.host.HostConfig;
import smart.api.host.HostConfigContext;
import smart.api.host.MonitorSystemHostConfig;
import smart.mast.action.Action;

public class CPUUsageListener extends AbstractListener {

	public CPUUsageListener(Cellet cellet) {
		super(cellet);
	}

	@Override
	public void onAction(ActionDialect action) {
		// 使用同步的方式进行请求
		// 注意：因为onAction方法是由Cell Cloud的action dialect进行回调的
		// 该方法独享一个线程，因此可以在此线程里进行阻塞式的调用
		// 因此，这里可以用同步的方式请求HTTP API

		// 获取参数
		JSONObject json = null;
		long moId = 0;
		int rangeInHour = 0;
		String eqType = null;

		try {
			json = new JSONObject(action.getParamAsString("data"));
			moId = json.getInt("moId");
			rangeInHour = json.getInt("rangeInHour");
			eqType = json.getString("eqType");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		System.out.println("参数1: " + moId + "  参数2： " + rangeInHour);
		// URL
		HostConfig cpuConfig = new MonitorSystemHostConfig();
		HostConfigContext context = new HostConfigContext(cpuConfig);
		StringBuilder url = new StringBuilder(context.getAPIHost()).append("/")
				.append(API.CPU).append("/").append(moId)
				.append("/fTotalCpu/?rangeInHour=").append(rangeInHour);

		// 创建请求
		Request request = this.getHttpClient().newRequest(url.toString());
		request.method(HttpMethod.GET);

		// 填写数据内容
		DeferredContentProvider dcp = new DeferredContentProvider();

		RequestContentCapsule capsule = new RequestContentCapsule();
		capsule.append("moId", moId);
		capsule.append("rangeInHour", rangeInHour);
		capsule.append("eqType", eqType);
		dcp.offer(capsule.toBuffer());
		dcp.close();
		request.content(dcp);

		// 发送请求
		ContentResponse response = null;

		try {
			response = request.send();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (TimeoutException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}

		Properties params = new Properties();
		JSONObject data = null;

		switch (response.getStatus()) {
		case HttpStatus.OK_200:
			byte[] bytes = response.getContent();
			if (null != bytes) {

				// 获取从Web服务器返回的数据
				String content = new String(bytes, Charset.forName("UTF-8"));

				try {
					data = new JSONObject(content);

					if ("success".equals(data.get("status"))) {
						if (!"".equals(data.get("dataList"))
								&& data.get("dataList") != null) {
							JSONArray ja = data.getJSONArray("dataList");
							DateFormat df = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");

							for (int i = 0; i < ja.length(); i++) {
								JSONObject jsonData = ja.getJSONObject(i);
								JSONArray ja1 = jsonData.getJSONArray("data");
								JSONArray ja2 = new JSONArray();

//								long cpuid = jsonData.getLong("mosn");
//								long timestamp = 0;
//								double usedPercent = 0;
								
								for (int j = 0; j < ja1.length(); j++) {
									JSONArray jsonData1 = ja1.getJSONArray(j);
									JSONObject jo = new JSONObject();

									if (null == jsonData1.get(0)
											|| "".equals(jsonData1.get(0))
											|| "null".equals(jsonData1.get(0))
											|| (jsonData1.get(0)).equals(null)) {
										jo.put("usage", 0);
									} else {
										jo.put("usage", Float
												.valueOf((String) jsonData1
														.get(0)));
									}
									jo.put("collectTime",
											df.parse((String) jsonData1.get(1))
													.getTime());
									ja2.put(jo);

//									usedPercent = Double
//											.valueOf((String) jsonData1.get(0));
//
//									timestamp = df.parse(
//											(String) jsonData1.get(1))
//											.getTime();
									
//									HostManager hm=HostManager.getInstance();
//									hm.addCPUPrecsById(cpuid, usedPercent, timestamp);

									System.out.println("CPUPrec___"+ja2);
								}

								jsonData.remove("data");
								jsonData.put("data", ja2);
								String s = jsonData.getString("moPath");
								jsonData.put("name", s.split("> ")[1]);
								jsonData.remove("kpi");
							}

							JSONObject jo = new JSONObject();
							jo.put("dataList", ja);
							jo.put("resourceId", moId);

							data.remove("dataList");
							data.put("data", jo);
							data.put("status", 300);
							data.put("errorInfo", "");
						}
					} else {
						data.put("errorInfo", "未获取到相关kpi数据");
					}

					System.out.println("data：      " + data);
					// 设置参数
					params.addProperty(new ObjectProperty("data", data));
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}

				// 响应动作，即向客户端发送ActionDialect
				// 参数tracker是一次动作的追踪标识符
				this.response(Action.CPU, params);
			} else {
				this.reportHTTPError(Action.CPU);
			}
			break;
		default:
			Logger.w(CPUUsageListener.class, "返回响应码:" + response.getStatus());

			try {
				data = new JSONObject();
				data.put("status", 900);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			// 设置参数
			params.addProperty(new ObjectProperty("data", data));

			// 响应动作，即向客户端发送 ActionDialect
			this.response(Action.CPU, params);
			break;
		}
	}
}
