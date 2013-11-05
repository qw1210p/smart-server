package smart.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import smart.bean.Equipment;
import smart.dao.AbstraceDao;
import smart.dao.EquipmentDao;
import smart.util.DButil;

/**
 * 设备DAO实现
 */
public class EquipmentDaoImpl extends AbstraceDao implements EquipmentDao {

	private PreparedStatement pstmt;
	private ResultSet rs;
	
	public EquipmentDaoImpl() {
		super();
	}

	@Override
	public void saveEquipment(Equipment e) {
		String sql = "insert into t_equipment values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			super.doStart();
			pstmt = super.conn.prepareStatement(sql);
			pstmt.setLong(1, e.getId());
			pstmt.setString(2, e.getTypeName());
			pstmt.setString(3, e.getVendor());
			pstmt.setLong(4, e.getTypeCode());
			pstmt.setString(5, e.getModel());
			pstmt.setString(6, e.getTypePath());
			pstmt.setString(7, e.getAlias());
			pstmt.setString(8, e.getMoIp());
			pstmt.setInt(9, e.getAlmStatus());
			pstmt.setInt(10, e.getMgrStatus());
			pstmt.setString(11, e.getMoName());
			pstmt.setString(12, e.getMoType());
			pstmt.setString(13, e.getMoSort());
			pstmt.executeUpdate();
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			DButil.getInstance().close(pstmt, null);
		}
	}

	@Override
	public boolean isExist(long equipmentId) {
		String sql = "select * from t_equipment where te_id = ?";
		boolean b = false;
		try {
			super.doStart();
			pstmt = super.conn.prepareStatement(sql);
			pstmt.setLong(1, equipmentId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				if (rs.getLong("te_id") == equipmentId) {
					b = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DButil.getInstance().close(pstmt, rs);
		}
		
		return b;
	}
}
