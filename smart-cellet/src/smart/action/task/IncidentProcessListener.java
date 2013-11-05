package smart.action.task;

import java.nio.charset.Charset;
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
import smart.api.host.ServiceDeskHostConfig;
import smart.mast.action.Action;


/** 
* @Description :  故障工单处理并保存操作
* @author      :  dwg
* @date        :  Oct 22, 2013 11:07:11 AM 
*  
*/
public final class IncidentProcessListener extends AbstractListener {


	public IncidentProcessListener(Cellet cellet) {
		super(cellet);
	}
	
	@Override
	public void onAction(ActionDialect action) {

		// 使用同步方式进行请求。
		// 因为 onAction 方法是由 Cell Cloud 的 action dialect 进行回调的，
		// 该方法独享一个线程，因此可以在此线程里进行阻塞式的调用。
		// 因此，可以用同步方式请求 HTTP API 。

		// URL
		HostConfig  serviceDeskConfig=new ServiceDeskHostConfig();
		HostConfigContext context=new HostConfigContext(serviceDeskConfig);
		StringBuilder url = new StringBuilder(context.getAPIHost()).append("/").append(API.INCIDENTPROCESS);
		
		// 创建请求
		Request request = this.getHttpClient().newRequest(url.toString());
		request.method(HttpMethod.GET);
		
		JSONObject json = null;
		String bpiId=null;

		String summary=null;
		String description=null;
		String occurTime=null;
		String contactId=null;
		String reportWays=null;
		String source=null;
		String applicant=null;
		String influencer=null;
		String urgent=null;
		String impct=null;
		String serviceLevel=null;
		String isMajor=null;
		String comment=null;
		String assigner=null;
		String investDiagn=null;
		String isSoluation=null;
		String reason=null;
		String resolution=null;
		String closeCode=null;
		
		try {
			json = new JSONObject(action.getParamAsString("data"));
			bpiId=json.getString("incidentId");
			 summary=json.getString("summary");
			 description=json.getString("description");
			 occurTime=json.getString("occurTime");
			 contactId=json.getString("contactId");
			 reportWays=json.getString("reportWays");
			 source=json.getString("source");
			 applicant=json.getString("applicant");
			 influencer=json.getString("influencer");
			 urgent=json.getString("urgent");
			 impct=json.getString("impact");
			 serviceLevel=json.getString("serviceLevel");
			 isMajor=json.getString("isMajor");
			 comment=json.getString("comment");
			 assigner=json.getString("assigner");
			 investDiagn=json.getString("investDiagn");
			 isSoluation=json.getString("isSoluation");
			 reason=json.getString("reason");
			 resolution=json.getString("resolution");
			 closeCode=json.getString("closeCode");
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
		
		// 填写数据内容
		DeferredContentProvider dcp = new DeferredContentProvider();

		RequestContentCapsule capsule = new RequestContentCapsule();
		capsule.append("bpiId", bpiId);
		capsule.append("summary", summary);
		capsule.append("description", description);
		capsule.append("occurTime", occurTime);
		capsule.append("contactId", contactId);
		capsule.append("reportWays", reportWays);
		capsule.append("source", source);
		capsule.append("applicant", applicant);
		capsule.append("influencer", influencer);
		capsule.append("urgent", urgent);
		capsule.append("impct", impct);
		capsule.append("serviceLevel", serviceLevel);
		capsule.append("isMajor", isMajor);
		capsule.append("comment", comment);
		capsule.append("assigner", assigner);
		capsule.append("investDiagn", investDiagn);
		capsule.append("isSoluation", isSoluation);
		capsule.append("reason", reason);
		capsule.append("resolution", resolution);
		capsule.append("closeCode", closeCode);
		
		dcp.offer(capsule.toBuffer());
		dcp.close();
		request.content(dcp);

		
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
				// 获取从 Web 服务器上返回的数据
				String content = new String(bytes, Charset.forName("UTF-8"));
				System.out.println("工单保存："+content);
				try {
					jo = new JSONObject(content);
					System.out.println("响应工单保存数据的返回值为：:" + jo);

					// 设置参数
					params.addProperty(new ObjectProperty("data", jo));

					// 响应动作，即向客户端发送 ActionDialect
					this.response(Action.INCIDENTPROCESS, params);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				this.reportHTTPError(Action.INCIDENTPROCESS);
			}

			break;
		default:
			Logger.w(IncidentListListener.class,
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
			this.response(Action.INCIDENTPROCESS, params);
			break;
		}
	}

}
