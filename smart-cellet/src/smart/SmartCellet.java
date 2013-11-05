package smart;

import net.cellcloud.common.LogLevel;
import net.cellcloud.common.Logger;
import net.cellcloud.core.Cellet;
import net.cellcloud.core.CelletFeature;
import net.cellcloud.talk.Primitive;
import net.cellcloud.talk.dialect.ActionDialect;
import net.cellcloud.talk.dialect.Dialect;

import org.eclipse.jetty.client.HttpClient;

import smart.action.ConnectionCheckListener;
import smart.action.LoginListener;
import smart.action.LogoutListener;
import smart.action.NodeDetectionListener;
import smart.action.alarm.AlarmCoverageListener;
import smart.action.alarm.AlarmDealListener;
import smart.action.alarm.AlarmDetailListener;
import smart.action.alarm.AlarmDistributionListener;
import smart.action.alarm.AlarmExperienceListener;
import smart.action.alarm.AlarmForwardListener;
import smart.action.alarm.AlarmLifeCycleListener;
import smart.action.alarm.AlarmListListener;
import smart.action.alarm.AlarmOpInfoListener;
import smart.action.alarm.DealInfoListener;
import smart.action.alarm.SuppressedAlarmListener;
import smart.action.attention.AddAttentionListener;
import smart.action.attention.AttentionListListener;
import smart.action.attention.CancelAttentionListener;
import smart.action.attention.DeleteAttentionListener;
import smart.action.ci.BusinessSystemListListener;
import smart.action.ci.CiDetailListener;
import smart.action.ci.CiListListener;
import smart.action.message.MessageContactsListener;
import smart.action.message.MessageCustomTagListener;
import smart.action.message.MessageDeleteListener;
import smart.action.message.MessageDetailListener;
import smart.action.message.MessageFileUploadListener;
import smart.action.message.MessageListListener;
import smart.action.message.MessageMarkListener;
import smart.action.message.MessageMoveListener;
import smart.action.message.MessageSendListener;
import smart.action.message.MessageTagAddListener;
import smart.action.message.MessageTagDeleteListener;
import smart.action.message.MessageTagDisplayListener;
import smart.action.message.MessageTagModifyListener;
import smart.action.message.MessageTopInfoListener;
import smart.action.resource.CPUUsageListener;
import smart.action.resource.DataBaseListener;
import smart.action.resource.DeleteEquipmentListener;
import smart.action.resource.EquipmentAlarmListener;
import smart.action.resource.EquipmentBasicListener;
import smart.action.resource.EquipmentHealthStatusListener;
import smart.action.resource.EquipmentListListener;
import smart.action.resource.EquipmentMonitorStateListener;
import smart.action.resource.EquipmentPerformanceListener;
import smart.action.resource.EquipmentTopoListener;
import smart.action.resource.FileSystemUsageListener;
import smart.action.resource.MemoryUsageListener;
import smart.action.resource.NetEquipmentListener;
import smart.action.resource.PingListener;
import smart.action.resource.SendSnapshotListener;
import smart.action.task.BpCloseCodeListListener;
import smart.action.task.BpiListListener;
import smart.action.task.IncidentCategoryListListener;
import smart.action.task.IncidentDetailListener;
import smart.action.task.IncidentListListener;
import smart.action.task.IncidentProcessListener;
import smart.action.task.OperationListListener;
import smart.action.task.SlaImpactListListener;
import smart.action.task.SlaServiceLevelListListener;
import smart.action.task.SlaUrgentListListener;
import smart.action.task.UserListListener;
import smart.action.time.CurrentTimeListener;
import smart.mast.Root;
import smart.mast.action.Action;
import smart.mast.action.ActionDispatcher;
import smart.mast.core.SystemCategory;

public class SmartCellet extends Cellet {

	private static SmartCellet instance;

	private HttpClient httpClient;

	public SmartCellet() {
		super(new CelletFeature("SmartITOM", new Version()));
		SmartCellet.instance = this;
		this.httpClient = new HttpClient();
	}

	public synchronized static SmartCellet getInstance() {
		return SmartCellet.instance;
	}

	@Override
	public void activate() {
		// 启动引擎
		Root.getInstance().startup(SystemCategory.MONITORING_SYSTEM, this);

		// 配置 Http 客户端
		this.configHttpClient();

		// 初始化监听器
		this.initListeners();
	}

	@Override
	public void deactivate() {
		try {
			this.httpClient.stop();
		} catch (Exception e) {
			Logger.log(this.getClass(), e, LogLevel.WARNING);
		}

		// 关闭引擎
		Root.getInstance().shutdown();
	}

	@Override
	public void dialogue(String tag, Primitive primitive) {
		if (primitive.isDialectal()) {
			Dialect dialect = primitive.getDialect();
			if (dialect.getName().equals(ActionDialect.DIALECT_NAME)) {
				ActionDispatcher.getInstance().dispatch(
						SystemCategory.MONITORING_SYSTEM,
						(ActionDialect) dialect);
			}
		}
	}

	@Override
	public void contacted(String tag) {
	}

	@Override
	public void quitted(String tag) {
	}

	@Override
	public void suspended(String tag) {
	}

	@Override
	public void resumed(String tag) {
	}

	/**
	 * 配置 HTTP 客户端。
	 */
	private void configHttpClient() {

		this.httpClient.setFollowRedirects(false);
		try {
			this.httpClient.start();
		} catch (Exception e) {
			Logger.log(this.getClass(), e, LogLevel.ERROR);
		}
	}

	/**
	 * 初始化所有监听器。
	 */
	private void initListeners() {
		ActionDispatcher dispatcher = ActionDispatcher.getInstance();

		// 服务器连接检测
		ConnectionCheckListener check = new ConnectionCheckListener(this);
		check.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.CONNECTIONCHECK, check);

		// 登录
		LoginListener login = new LoginListener(this);
		login.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.LOGIN, login);

		// 获取关注列表
		AttentionListListener attentionList = new AttentionListListener(this);
		attentionList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ATTENTIONLIST, attentionList);

		// 取消关注
		CancelAttentionListener cancelAttention = new CancelAttentionListener(
				this);
		cancelAttention.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.CANCELATTENTION, cancelAttention);

		AttentionListListener al = new AttentionListListener(this);
		al.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.CANCELATTENTION, al);

		// 删除关注
		DeleteAttentionListener deleteAttention = new DeleteAttentionListener(
				this);
		deleteAttention.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.DELETEATTENTION, deleteAttention);

		AttentionListListener al1 = new AttentionListListener(this);
		al1.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.DELETEATTENTION, al1);

		// 添加关注
		AddAttentionListener addAttention = new AddAttentionListener(this);
		addAttention.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ADDATTENTION, addAttention);

//		AttentionListListener al2 = new AttentionListListener(this);
//		al2.setHttpClient(this.httpClient);
//		dispatcher.addListener(Action.ADDATTENTION, al2);

		// 获取消息
		MessageListListener messageList = new MessageListListener(this);
		messageList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGELIST, messageList);

		// 获取消息详细
		MessageDetailListener messageDetailListener = new MessageDetailListener(
				this);
		messageDetailListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGEDETAIL, messageDetailListener);

		// 删除消息
		MessageDeleteListener MessageDeleteListener = new MessageDeleteListener(
				this);
		MessageDeleteListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGEDELETE, MessageDeleteListener);

		// 转移消息
		MessageMoveListener MessageMoveListener = new MessageMoveListener(this);
		MessageMoveListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGEMOVE, MessageMoveListener);

		// 标记消息
		MessageMarkListener MessageMarkListener = new MessageMarkListener(this);
		;
		MessageMarkListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGEMARK, MessageMarkListener);

		// 获取联系人
		MessageContactsListener messageContactsListener = new MessageContactsListener(
				this);
		messageContactsListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGECONTACTS, messageContactsListener);

		// 发送消息
		MessageSendListener messageSendListener = new MessageSendListener(this);
		messageSendListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGESEND, messageSendListener);

		// 消息置顶
		MessageTopInfoListener messageTopInfoListener = new MessageTopInfoListener(
				this);
		messageTopInfoListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGETOPINFO, messageTopInfoListener);

		// 添加消息标签
		MessageTagAddListener messageTagAddListener = new MessageTagAddListener(
				this);
		messageTagAddListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGETAGADD, messageTagAddListener);

		// 删除消息标签
		MessageTagDeleteListener messageTagDeleteListener = new MessageTagDeleteListener(
				this);
		messageTagDeleteListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGETAGDELETE,
				messageTagDeleteListener);

		// 修改消息标签
		MessageTagModifyListener messageTagModifyListener = new MessageTagModifyListener(
				this);
		messageTagModifyListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGETAGMODIFY,
				messageTagModifyListener);

		// 设置消息标签为显示或隐藏状态
		MessageTagDisplayListener messageTagDisplayListener = new MessageTagDisplayListener(
				this);
		messageTagDisplayListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGETAGDISPLAY,
				messageTagDisplayListener);

		// 获取所有自定义标签
		MessageCustomTagListener messageCustomTagListener = new MessageCustomTagListener(
				this);
		messageCustomTagListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGECUSTOMTAGS,
				messageCustomTagListener);

		// 上传附件
		MessageFileUploadListener messageFileUploadListener = new MessageFileUploadListener(
				this);
		messageFileUploadListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.MESSAGEFILEUPLOAD,
				messageFileUploadListener);

		// 获取所有设备列表
		EquipmentListListener equipmentList = new EquipmentListListener(this);
		equipmentList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.DEVICE, equipmentList);

		// 获取数据库详细
		DataBaseListener db = new DataBaseListener(this);
		db.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.DATABASE, db);

		// 获取主机设备
		CPUUsageListener cu = new CPUUsageListener(this);
		cu.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.HOST, cu);

		MemoryUsageListener memu = new MemoryUsageListener(this);
		memu.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.HOST, memu);

		FileSystemUsageListener filesu = new FileSystemUsageListener(this);
		filesu.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.HOST, filesu);

		PingListener ping = new PingListener(this);
		ping.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.HOST, ping);

		// 获取网络设备
		NetEquipmentListener netEqpt = new NetEquipmentListener(this);
		netEqpt.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.NETEQUIPMENT, netEqpt);

		// 获取设备基本信息
		EquipmentBasicListener eqptBasicListener = new EquipmentBasicListener(
				this);
		eqptBasicListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.EQUIPMENTBASIC, eqptBasicListener);

		// 获取设备性能信息
		EquipmentPerformanceListener eqptPerformance = new EquipmentPerformanceListener(
				this);
		eqptPerformance.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.EQUIPMENTDETAIL, eqptPerformance);

		// 获取设备健康状态
		EquipmentHealthStatusListener healthStatus = new EquipmentHealthStatusListener(
				this);
		healthStatus.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.EQUIPMENTDETAIL, healthStatus);

		// 删除设备
		DeleteEquipmentListener equipmentDelete = new DeleteEquipmentListener(
				this);
		equipmentDelete.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.DELETEEQUIPMENT, equipmentDelete);

		// 获取设备告警
		EquipmentAlarmListener eqptAlarmListener = new EquipmentAlarmListener(
				this);
		eqptAlarmListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.EQUIPMENTALARM, eqptAlarmListener);

		// 改变设备监控状态
		EquipmentMonitorStateListener eqptChangeMonitorStatusListener = new EquipmentMonitorStateListener(
				this);
		eqptChangeMonitorStatusListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.EQUIPMENTMONITORSTATE,
				eqptChangeMonitorStatusListener);

		// 发送快照
		SendSnapshotListener sendSnapshot = new SendSnapshotListener(this);
		sendSnapshot.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.SENDSNAPSHOT, sendSnapshot);

		// 获取设备拓扑
		EquipmentTopoListener eqptTopoListener = new EquipmentTopoListener(this);
		eqptTopoListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.EQUIPMENTTOPO, eqptTopoListener);

		// 注销
		LogoutListener logout = new LogoutListener(this);
		logout.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.LOGOUT, logout);

		// 当前时间
		CurrentTimeListener currentTime = new CurrentTimeListener(this);
		currentTime.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.CURRENTTIME, currentTime);

		// 获取告警基本信息
		AlarmDetailListener alarmDetail = new AlarmDetailListener(this);
		alarmDetail.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMDETAIL, alarmDetail);

		// 告警操作
		AlarmDealListener alarmDeal = new AlarmDealListener(this);
		alarmDeal.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMDEAL, alarmDeal);

		AlarmDetailListener basicInfo = new AlarmDetailListener(this);
		basicInfo.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMDEAL, basicInfo);

		// 告警转发
		AlarmForwardListener alarmForward = new AlarmForwardListener(this);
		alarmForward.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMFORWARD, alarmForward);

		// 添加告警处理信息
		DealInfoListener dealInfo = new DealInfoListener(this);
		dealInfo.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.DEALINFO, dealInfo);

		// 获取告警列表
		AlarmListListener alarmList = new AlarmListListener(this);
		alarmList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMLIST, alarmList);

		// 获取告警处理信息
		AlarmOpInfoListener alarmOpInfo = new AlarmOpInfoListener(this);
		alarmOpInfo.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMDETAIL, alarmOpInfo);

		// 获取告警维护经验
		AlarmExperienceListener alarmExperience = new AlarmExperienceListener(
				this);
		alarmExperience.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMDETAIL, alarmExperience);

		// 获取告警影响范围
		AlarmCoverageListener alarmCoverage = new AlarmCoverageListener(this);
		alarmCoverage.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMDETAIL, alarmCoverage);

		// 获取告警生命周期
		AlarmLifeCycleListener lifeCycle = new AlarmLifeCycleListener(this);
		lifeCycle.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMDETAIL, lifeCycle);
		// 获取压制的告警
		SuppressedAlarmListener suppressed = new SuppressedAlarmListener(this);
		suppressed.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMDETAIL, suppressed);

		// 获取告警分布
		AlarmDistributionListener distribution = new AlarmDistributionListener(
				this);
		distribution.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.ALARMDETAIL, distribution);

		// 节点监测
		NodeDetectionListener nodeDec = new NodeDetectionListener(this);
		nodeDec.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.NODEDETECTION, nodeDec);

		/************** 待办 *************/
		// 获取故障列表
		IncidentListListener incidentList = new IncidentListListener(this);
		incidentList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.INCIDENTLIST, incidentList);

		// 获取指定故障任务工单明细
		IncidentDetailListener incidentDetail = new IncidentDetailListener(this);
		incidentDetail.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.INCIDENTDETAIL, incidentDetail);
		
		// 获取任务操作项
		OperationListListener operationList = new OperationListListener(this);
		operationList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.LISTOPERATION, operationList);
		
		// 故障任务单处理
		IncidentProcessListener incidentProcess = new IncidentProcessListener(this);
		incidentProcess.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.INCIDENTPROCESS, incidentProcess);
		
		// 获取故障类型
		IncidentCategoryListListener incidentCategory = new IncidentCategoryListListener(this);
		incidentCategory.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.INCIDENTCATEGORY, incidentCategory);
		
		// 获取所有流程工单列表
		BpiListListener bpiList = new BpiListListener(this);
		bpiList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.BPILIST, bpiList);
		
		// 获取用户列表
		UserListListener userList = new UserListListener(this);
		userList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.COMMONUSERLIST, userList);
	
		//影响程度
		SlaImpactListListener impactList = new SlaImpactListListener(this);
		impactList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.IMPACTLIST, impactList);
		
		//紧急程度
		SlaUrgentListListener urgentList = new SlaUrgentListListener(this);
		urgentList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.URGENTLIST, urgentList);
		
		//优先级
		SlaServiceLevelListListener serviceLevelList = new SlaServiceLevelListListener(this);
		serviceLevelList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.SERVICELEVELLIST, serviceLevelList);
		
		//关闭代码
		BpCloseCodeListListener closeCodeList = new BpCloseCodeListListener(this);
		impactList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.CLOSECODELIST, closeCodeList);
		
		/*************** 资产管理 **************/
		// 获取设备资产列表数据
		CiListListener ciListener = new CiListListener(this);
		ciListener.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.CILIST, ciListener);
		
		// 获取所有业务系统列表
		BusinessSystemListListener bizSystemList = new BusinessSystemListListener(this);
		bizSystemList.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.BIZSYSTEMLIST, bizSystemList);

		// 获取指定设备资产详细信息
		CiDetailListener ciDetail = new CiDetailListener(this);
		ciDetail.setHttpClient(this.httpClient);
		dispatcher.addListener(Action.CIDETAIL, ciDetail);
		
	}
}
