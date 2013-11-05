package smart.core;

import org.json.JSONException;
import org.json.JSONObject;

import smart.bean.Equipment;
import smart.dao.EquipmentDao;
import smart.dao.impl.EquipmentDaoImpl;

/**
 * 设备管理器
 */
public class EquipmentManager {

	// 设备单例
	private static final EquipmentManager instance = new EquipmentManager();
	private EquipmentDao dao;
	
	private EquipmentManager() {
		dao = new EquipmentDaoImpl();
	}

	public static EquipmentManager getInstance() {
		return EquipmentManager.instance;
	}
	
	// 保存设备信息
	public void storeEquipment(JSONObject jo) {
		
		Equipment e = null;
		try {
			e = new Equipment(jo.getLong("moId"));
			e.setMoSort(jo.getString("moSort"));
			e.setTypeCode(jo.getLong("typeCode"));
			e.setTypeName(jo.getString("typeName"));
			JSONObject json = jo.getJSONObject("base_info");
			e.setModel(json.getString("model"));
			e.setTypePath(json.getString("typePath"));
			e.setAlias(json.getString("alias"));
			e.setVendor(json.getString("vendor"));
			e.setMoIp(json.getString("moIp"));
			e.setMoType(json.getString("moType"));
			e.setAlmStatus(json.getInt("almStatus"));
			e.setMgrStatus(json.getInt("mgrStatus"));
			e.setMoName(json.getString("moName"));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		this.dao.saveEquipment(e);
	}
	
	public boolean isExist(long equipmentId) {
		boolean b = this.dao.isExist(equipmentId);
		return b;
	}
}
