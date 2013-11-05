package smart.action.alarm;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
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
 * 告警列表监听
 */
public final class AlarmListListener extends AbstractListener {

	public AlarmListListener(Cellet cellet) {
		super(cellet);
	}

	@Override
	public void onAction(ActionDialect action) {

		// 使用同步方式进行请求
		// 因为onAction方法是由cell cloud 的action dialect 进行回调的，
		// 该方法独享一个线程，因此可以在此线程里进行阻塞式的调用。
		// 设置请求HTTP API方式

		// URL
		HostConfig config = new MonitorSystemHostConfig();
		HostConfigContext context = new HostConfigContext(config);
		StringBuilder url = new StringBuilder(context.getAPIHost()).append("/")
				.append(API.ALARMLIST);

		// 创建请求
		Request request = this.getHttpClient().newRequest(url.toString());
		request.method(HttpMethod.GET);
		url = null;

		// 获取请求参数
		JSONObject json = null;
		String token = null;
		int currentIndex = 0;
		int pagesize = 0;
		try {
			json = new JSONObject(action.getParamAsString("data"));
			token = json.getString("token");
			currentIndex = json.getInt("currentIndex");
			pagesize = json.getInt("pagesize");

			// 填写数据内容
			DeferredContentProvider dcp = new DeferredContentProvider();
			RequestContentCapsule capsule = new RequestContentCapsule();
			capsule.append("token", token);
			capsule.append("currentIndex", currentIndex);
			capsule.append("pagesize", pagesize);
			for (Iterator<Object> iter = json.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				if (key.contains("sortby")) {
					String sortby = null;
					sortby = json.getString("sortby");
					capsule.append("sortby", sortby);
				} else if (key.contains("filterType")) {
					String filterType = null;
					filterType = json.getString("filterType");
					capsule.append("filterType", filterType);
				} else if (key.contains("condition")) {
					String condition = null;
					condition = json.getString("condition");
					capsule.append("condition", condition);
				}
			}
			dcp.offer(capsule.toBuffer());
			dcp.close();
			request.content(dcp);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		Properties params = new Properties();

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
					if ("成功".equals(jo.get("result"))) {
						ja = jo.getJSONArray("almlist");
						JSONArray jar = new JSONArray();
						List<String> list = Arrays.asList("almId", "moId",
								"rootMoId", "parentMoId", "typeCode",
								"almCause", "isSuppressed", "severity",
								"extraInfo", "almStatus", "trend", "occurTime",
								"lastTime", "count", "detail", "originalInfo",
								"confirmTime", "confirmUserId", "confirmUser",
								"moIp", "moName", "causeAlias", "location");
						if (ja.length() > 18) {
							for (int i = 0; i < 18; i++) {
								JSONObject job = new JSONObject();

								for (int j = 0; j < ja.getJSONArray(i).length(); j++) {
									for (int k = 0; k < list.size(); k++) {
										if (k == j) {
											String key = list.get(k);
											String value = ja.getJSONArray(i)
													.get(j).toString();
											if ("almId".equals(key)
													|| "moId".equals(key)
													|| ("confirmUserId"
															.equals(key) && (!""
															.equals(value)))) {
												job.put(key,
														Long.valueOf(value));
											} else if ("occurTime".equals(key)
													|| "lastTime".equals(key)
													|| ("confirmTime"
															.equals(key) && !""
															.equals(value))) {
												DateFormat df = new SimpleDateFormat(
														"yyyy-MM-dd HH:mm:ss");
												Date date = null;
												date = df.parse(value);
												job.put(key, date.getTime());
											} else if ("count".equals(key)
													|| "severity".equals(key)) {
												job.put(key,
														Integer.parseInt(value));
											} else if (("confirmUserId"
													.equals(key) || "confirmTime"
													.equals(key))
													&& "".equals(value)) {
												job.put(key, 0);
											} else {
												job.put(key, value);
											}
										}
									}
								}
								jar.put(job);
//								if (DButil.getInstance().getConnection() != null) {
//									boolean b = AlarmManager.getInstance().isExist(job.getLong("almId"));
//									if (!b){
//										AlarmManager.getInstance().signInList(job);
//									}
//								}
							}
						}
						System.out.println("jsonArray" + jar.length()+": " + jar);
						jo.remove("result");
						jo.remove("almlist");
						jo.put("almList", jar);
						jo.put("status", 300);
						jo.put("errorInfo", "");
					} else {
						jo.remove("result");
						jo.remove("almlist");
						jo.put("almList", "");
						jo.put("status", 301);
						jo.put("errorInfo", "找不到符合条件的相关告警列表");
					}
					
					// 设置参数
					params.addProperty(new ObjectProperty("data", jo));

					// 响应动作，即向客户端发送ActionDialect
					// 参数tracker是一次动作的追踪标识，这里可以使用访问标记token
					this.response(Action.ALARMLIST, params);

				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				this.reportHTTPError(Action.ALARMLIST);
			}
			break;
		default:
			Logger.w(AlarmListListener.class, "返回响应码：" + response.getStatus());
			jo = new JSONObject();
			try {
				jo.put("status", 900);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// 设置参数
			params.addProperty(new ObjectProperty("data", jo));

			// 响应动作，即向客户端发送 ActionDialect
			this.response(Action.ALARMLIST, params);
			break;
		}
	}
}
