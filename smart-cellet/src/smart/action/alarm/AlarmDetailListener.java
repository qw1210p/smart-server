package smart.action.alarm;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

/**
 * 告警基本信息监听
 */
public final class AlarmDetailListener extends AbstractListener {

	public AlarmDetailListener(Cellet cellet) {
		super(cellet);
	}

	@Override
	public void onAction(ActionDialect action) {

		// 使用同步方式进行请求
		// 因为onAction方法是由cell cloud 的action dialect 进行回调的，
		// 该方法独享一个线程，因此可以在此线程里进行阻塞式的调用。
		// 设置请求HTTP API方式

		// 获取请求参数
		JSONObject json;
		String token = null;
		long almId = 0;
		try {
			json = new JSONObject(action.getParamAsString("data"));
			token = json.getString("token");
			almId = json.getLong("almId");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		Properties params = new Properties();
		if (token != null && !"".equals(token)) {

			// URL
			HostConfig config = new MonitorSystemHostConfig();
			HostConfigContext context = new HostConfigContext(config);
			StringBuilder url = new StringBuilder(context.getAPIHost())
					.append("/").append(API.ALARMDETAIL).append("/")
					.append(almId);

			// 创建请求
			Request request = this.getHttpClient().newRequest(url.toString());
			request.method(HttpMethod.GET);

			// 填写数据内容
			DeferredContentProvider dcp = new DeferredContentProvider();
			RequestContentCapsule capsule = new RequestContentCapsule();
			capsule.append("token", token);
			capsule.append("almId", almId);
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

			JSONObject jo = null;
			switch (response.getStatus()) {
			case HttpStatus.OK_200:
				byte[] bytes = response.getContent();
				if (null != bytes) {

					// 获取从Web服务器上返回的数据
					String content = new String(bytes, Charset.forName("UTF-8"));
					try {
						jo = new JSONObject(content);

						JSONArray ja = new JSONArray();
						if (jo.has("result")&&"成功".equals(jo.get("result"))) {
							ja = jo.getJSONArray("almlist");
							List<String> list = Arrays.asList("almId", "moId",
									"rootMoId", "parentMoId", "typeCode",
									"almCause", "isSuppressed", "severity",
									"extraInfo", "almStatus", "trend",
									"occurTime", "lastTime", "count", "detail",
									"originalInfo", "confirmTime",
									"confirmUserId", "confirmUser", "moIp",
									"moName", "causeAlias", "location");
							JSONObject job = new JSONObject();
							for (int i = 0; i < ja.length(); i++) {
								for (int j = 0; j < ja.getJSONArray(i).length(); j++) {
									for (int k = 0; k < list.size(); k++) {
										if (k == j) {
											String key = list.get(k);
											String value = ja.getJSONArray(i).getString(j);
											if ("almId".equals(key) || "moId".equals(key)
													|| ("confirmUserId".equals(key) 
															&& !"".equals(value))) {
												job.put(key, Long.valueOf(value));
											} else if ("occurTime".equals(key)
													|| "lastTime".equals(key)
													|| ("confirmTime".equals(key) 
															&& !"".equals(value))) {
												DateFormat df = new SimpleDateFormat(
														"yyyy-MM-dd HH:mm:ss");
												Date date = null;
												date = df.parse(value);
												job.put(key, date.getTime());
											} else if ("count".equals(key)
													|| "severity".equals(key)) {
												job.put(key, Integer.parseInt(value));
											} else if (("confirmUserId".equals(key) 
													|| "confirmTime".equals(key))
													&& "".equals(value)) {
												job.put(key, 0);
											} else {
												job.put(key, value);
											}
										}
									}
								}
							}
							System.out.println("jsonObject:" + job);
							jo.remove("result");
							jo.remove("almlist");
							jo.put("category", "baseInfo");
							jo.put("almId", almId);
							jo.put("status", 300);
							jo.put("baseInfo", job);
							jo.put("errorInfo", "");
						} else {
							jo.put("category", "baseInfo");
							jo.put("almId", almId);
							jo.put("status", 311);
							jo.put("baseInfo", "");
							jo.put("errorInfo", "找不到符合条件的相关告警详情");
						}
						
						// 设置参数
						params.addProperty(new ObjectProperty("data", jo));

						// 响应动作，即向客户端发送ActionDialect
						// 参数tracker是一次动作的追踪标识，这里可以使用访问标记token
						Logger.i(this.getClass(), params.toString());
						this.response(token, Action.ALARMDETAIL, params);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else {
					this.reportHTTPError(token, Action.ALARMDETAIL);
				}
				break;
			default:
				Logger.w(AlarmDetailListener.class,
						"返回响应码：" + response.getStatus());
				jo = new JSONObject();
				try {
					jo.put("status", 900);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// 设置参数
				params.addProperty(new ObjectProperty("data", jo));

				// 响应动作，即向客户端发送 ActionDialect
				this.response(token, Action.ALARMDETAIL, params);
				break;
			}
		} else {
			JSONObject jo = new JSONObject();
			try {
				jo.put("status", 800);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			params.addProperty(new ObjectProperty("data", jo));
			this.response(Action.ALARMDETAIL, params);
		}
	}
}
