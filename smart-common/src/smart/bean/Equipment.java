package smart.bean;

import smart.entity.AbstractEntity;

/**
 * 设备
 */
public class Equipment extends AbstractEntity {

	private static final long serialVersionUID = 3223264554610562003L;

	private String typeName;
	private String vendor;
	private long typeCode;
	private String model;
	private String typePath;
	private String alias;
	private String moIp;
	private int almStatus;
	private int mgrStatus;
	private String moName;
	private String moType;
	private String moSort;
	
	public Equipment(long id) {
		super(id);
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public long getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(long typeCode) {
		this.typeCode = typeCode;
	}

	public String getMoSort() {
		return moSort;
	}

	public void setMoSort(String moSort) {
		this.moSort = moSort;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getTypePath() {
		return typePath;
	}

	public void setTypePath(String typePath) {
		this.typePath = typePath;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getMoIp() {
		return moIp;
	}

	public void setMoIp(String moIp) {
		this.moIp = moIp;
	}

	public int getAlmStatus() {
		return almStatus;
	}

	public void setAlmStatus(int almStatus) {
		this.almStatus = almStatus;
	}

	public int getMgrStatus() {
		return mgrStatus;
	}

	public void setMgrStatus(int mgrStatus) {
		this.mgrStatus = mgrStatus;
	}

	public String getMoName() {
		return moName;
	}

	public void setMoName(String moName) {
		this.moName = moName;
	}

	public String getMoType() {
		return moType;
	}

	public void setMoType(String moType) {
		this.moType = moType;
	}

}
