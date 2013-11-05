package smart.action.alarm;

import java.nio.charset.Charset;
import java.util.Date;
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
 * 告警处理监听
 */
public final class AlarmDealListener extends AbstractListener {

	public AlarmDealListener(Cellet cellet) {
		super(cellet);
	}

	@Override
	public void onAction(ActionDialect action) {

		// 使用同步方式请求
		// 因为onAction()方法是由cell cloud的action dialect进行回调的
		// 该方法独享一个线程，因此可以在此线程里进行阻塞式调用
		// 因此，使用同步方式请求HTTP API

		// 获取请求参数
		JSONObject json;
		String token = null;
		long almId = 0;
		String opType = null;
		String username = null;
//		long userId = 0;
		try {
			json = new JSONObject(action.getParamAsString("data"));
			System.out.println("操作参数："+json);
			token = json.getString("token");
			username = json.getString("username");
//			userId = json.getLong("userId");
			almId = json.getLong("almId");
			opType = json.getString("opType");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		Properties params = new Properties();
		if (token != null && !"".equals(token)) {

			// URL
			HostConfig config = new MonitorSystemHostConfig();
			HostConfigContext context = new HostConfigContext(config);
			
			// 创建请求
			Request request = null;
//			String s = almIdList.toString().substring(1).replace("]", "");
			StringBuilder url = null;
			if ("almConfirm".equals(opType)) {
				url = new StringBuilder(context.getAPIHost())
				.append("/").append(API.ALARMDEAL).append("/confirm/").append(almId)
				.append("?DMSN=101&userID=").append(almId).append("&userName=").append(username);
			} else if ("almDel".equals(opType)) {
				url = new StringBuilder(context.getAPIHost())
				.append("/").append(API.ALARMDEAL).append("/clear/").append(almId)
				.append("?DMSN=101&userID=").append(almId).append("&userName=").append(username);
			}
			request = this.getHttpClient().newRequest(url.toString());
			request.method(HttpMethod.GET);

			// 填写数据内容
//			String username = UserManager.getInstance().getUsername(token);
			long opTime = new Date().getTime();
			DeferredContentProvider dcp = new DeferredContentProvider();
			RequestContentCapsule capsule = new RequestContentCapsule();
			capsule.append("username", username);
			capsule.append("almId", almId);
			capsule.append("opTime", opTime);
			capsule.append("opType", opType);
			capsule.append("token", token);
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

			switch (response.getStatus()) {
			case HttpStatus.OK_200:
				JSONObject jo = null;
				byte[] bytes = response.getContent();
				if (null != bytes) {

					// 获取从Web服务器上返回的数据
					String content = new String(bytes, Charset.forName("UTF-8"));
					try {
						jo = new JSONObject(content);

						if ("成功".equals(jo.getString("result"))) {
//							if (DButil.getInstance().getConnection() != null) {
//								AlarmManager.getInstance().alarmDeal(almId,
//										opType, username, userId, opTime);
//							}
							jo.remove("result");
							jo.put("status", 300);
							jo.put("errorInfo", "");
						} else {
							jo.remove("result");
							jo.put("status", 386);
							jo.put("errorInfo", jo.get("msg"));
						}
						
						// 设置参数
						params.addProperty(new ObjectProperty("data", jo));

						// 响应动作，即向客户端发送ActionDialect
						// 参数tracker 是一次动作的追踪标识，这里可以直接使用访问标识token
						this.response(token, Action.ALARMDEAL, params);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					this.reportHTTPError(Action.ALARMDEAL);
				}
				break;
			default:
				Logger.w(AlarmDealListener.class,
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
				// 参数 tracker 是一次动作的追踪标识，这里使用操作类型。
				this.response(Action.ALARMDEAL, params);
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
			this.response(Action.ALARMDEAL, params);
		}
	}
}