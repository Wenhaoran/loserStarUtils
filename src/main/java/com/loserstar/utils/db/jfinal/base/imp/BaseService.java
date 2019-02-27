package com.loserstar.utils.db.jfinal.base.imp;

import java.util.List;
import java.util.UUID;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.loserstar.utils.idgen.SnowflakeIdWorker;

/**
 * 
 * author: loserStar
 * date: 2019年2月27日下午4:37:29
 * remarks:基础service，修复BUG，软删除条件del字段假如数据库是字符类型，这边因为没加单引号导致sql错误
 */
public  abstract class BaseService {
	public enum DBType{
		mysql,db2,oracle,sqlserver
	}
	/**
	 * 删除
	 */
	private static final String DEL = "1";
	/**
	 * 未删除
	 */
	private static final String NOT_DEL = "0";
	
	/**
	 * 批量curd时的标记名称
	 */
	public static final String EDIT_FLAG = "curd_flag";
	public static final String EDIT_FLAG_C = "c";
	public static final String EDIT_FLAG_U = "u";
	public static final String EDIT_FLAG_R = "r";
	public static final String EDIT_FLAG_D = "d";
	/**
	 * 返回具体表名称
	 * @return
	 */
	protected abstract String getTableName();
	/**
	 * 返回表对应的主键名称
	 * @return
	 */
	protected abstract String getPrimaryKey();
	
	protected String dataSourceName;//jfinal使用多数据源时候指定数据源名称
	
	/**
	 * 返回该表软删除使用的字段，查询列表的方法会自动过滤该字段值(如果未指定该字段(null或者空字符串)，则认为该表不具有软删除功能)
	 * @return
	 */
	protected abstract String getSoftDelField();
	/**
	 * 构造方法检查该有的变量是否有
	 */
	public BaseService() {
		try {
			if (getTableName()==null||getTableName().equals("")) {
				throw new Exception(this.getClass().getName()+" 中的getTableName()方法没有指定表名称");
			}
			if (getPrimaryKey()==null||getPrimaryKey().equals("")) {
				throw new Exception(this.getClass().getName()+" 中的getPrimaryKey()方法没有指定表的主键名称");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public BaseService(String dataSourceName) {
		try {
			if (getTableName()==null||getTableName().equals("")) {
				throw new Exception(this.getClass().getName()+" 中的getTableName()方法没有指定表名称");
			}
			if (getPrimaryKey()==null||getPrimaryKey().equals("")) {
				throw new Exception(this.getClass().getName()+" 中的getPrimaryKey()方法没有指定表的主键名称");
			}
			this.dataSourceName = dataSourceName;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 检查whereHelper是否为null,是旧返回空字符串，不是就加上where 并且toString
	 * @param whereHelper
	 * @return
	 */
	private String CheckWhereHelper(WhereHelper whereHelper) {
		String result = "";
		if(whereHelper!=null) {
			if (whereHelper.getStrWhereList()!=null&&whereHelper.getStrWhereList().size()>0) {
				result = " where "+whereHelper.toString();
			}else {
				 result+=" "+(whereHelper.getOrderStr()==null?"":whereHelper.getOrderStr());
			}
		}
		return  result;
	}
	
	/**
	 * 检查软删除字段是否存在，存在返回true，不存在返回false
	 * @return
	 */
	private boolean CheckSoftDelField() {
		if(getSoftDelField()==null||getSoftDelField().equals("")) {
			return false;
		}else {
			return true;
		}
	}
	/**
	 * 检测是否指定jfinal的数据源
	 * @return 指定返回true，未指定返回false
	 */
	private boolean CheckDataSourceName() {
		if (this.dataSourceName==null||this.dataSourceName.equals("")) {
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * 添加softDelField的过滤条件
	 * @param whereHelper
	 * @return
	 */
	private WhereHelper addSoftDelField(WhereHelper whereHelper) {
		if (CheckSoftDelField()) {
			if (whereHelper==null) {
				whereHelper = new WhereHelper();
			}
			whereHelper.addStrWhere(" and ("+getSoftDelField()+"= '"+NOT_DEL+"' or "+getSoftDelField()+" is null)");
		}
		return whereHelper;
	}
	
	/**
	 * 根据sql得到单调数据（效率慢，因为是取所有数据出来再取第一条）
	 * @see com.loserstar.utils.db.jfinal.base.imp.BaseService.getFirstList(WhereHelper, DBType)
	 * @param sql
	 * @return
	 */
	public Record get(String sql) {
		List<Record> list = CheckDataSourceName()?Db.use(this.dataSourceName).find(sql):Db.find(sql);
		if (list!=null&&list.size()>0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	/**
	 * 根据sql得到单调数据(参数化查询方式，效率慢，因为是取所有数据出来再取第一条)
	 * @see com.loserstar.utils.db.jfinal.base.imp.BaseService.getFirstList(WhereHelper, DBType)
	 * @param sql
	 * @return
	 */
	public Record get(String sql,Object... paras) {
		List<Record> list = CheckDataSourceName()?Db.use(this.dataSourceName).find(sql, paras):Db.find(sql,paras);
		if (list!=null&&list.size()>0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	/**
	 * 统计数量
	 * @param whereHelper
	 * @return
	 */
	public int getCount(WhereHelper whereHelper) {
		String sql ="select COUNT("+getPrimaryKey()+") count from "+getTableName()+CheckWhereHelper(whereHelper);
		return get(sql).getInt("count");
	}
	
	/**
	 * 设置某条记录的某个字段为null值
	 * @param tableName 表名
	 * @param fieldName 要设为null的字段名
	 * @param primaryId 主键ID的值
	 * @return
	 */
	public boolean updateFieldIsNull(String fieldName,String primaryId) {
		String sql = "UPDATE "+getTableName()+" SET "+fieldName+" = NULL WHERE "+getPrimaryKey()+"='"+primaryId+"'";
		int row = CheckDataSourceName()?Db.use(this.dataSourceName).update(sql):Db.update(sql);
		if (row<1) {
			return false;
		}
		return true;
	}
	public boolean updateFieldIsNull(String fieldName,long primaryId) {
		String sql = "UPDATE "+getTableName()+" SET "+fieldName+" = NULL WHERE "+getPrimaryKey()+"="+primaryId+"";
		int row = CheckDataSourceName()?Db.use(this.dataSourceName).update(sql):Db.update(sql);
		if (row<1) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 查询列表
	 * new一个whereHelper参数：如果设置过软删除字段自动过滤
	 * null:直接不添加软删除过滤
	 * @param whereHelper 查询条件
	 * @return
	 */
	public List<Record> getList(WhereHelper whereHelper){
		addSoftDelField(whereHelper);
		return getList_notSoftDel(whereHelper);
	}
	
	/**
	 * 查询列表(不自动添加软删除过滤)
	 * @param whereHelper
	 * @return
	 */
	public List<Record> getList_notSoftDel(WhereHelper whereHelper){
		String sql ="select * from "+getTableName()+CheckWhereHelper(whereHelper);
		return CheckDataSourceName()?Db.use(this.dataSourceName).find(sql):Db.find(sql);
	}
	
	/**
	 * 多表连接查询，默认查询出的字段使用*（此方法会造成如果两张表有相同名称的字段，会显示不全）
	 * @param joinHelper
	 * @param whereHelper
	 * @return
	 */
	public List<Record> getJoinList(JoinHelper joinHelper,WhereHelper whereHelper) {
		return getJoinList(null, joinHelper, whereHelper);
	}
	
	/**
	 * 多表连接查询，并且指定查询出的字段名称(不自动添加软删除过滤)
	 * @param selectFiled
	 * @param joinHelper
	 * @param whereHelper
	 * @return
	 */
	public List<Record> getJoinList(String selectFiled,JoinHelper joinHelper,WhereHelper whereHelper) {
		addSoftDelField(whereHelper);
		return getJoinList_notSoftDel(selectFiled,joinHelper,whereHelper);
	}
	
	/**
	 *  多表连接查询，并且指定查询出的字段名称
	 * @param selectFiled
	 * @param joinHelper
	 * @param whereHelper
	 * @return
	 */
	public List<Record> getJoinList_notSoftDel(String selectFiled,JoinHelper joinHelper,WhereHelper whereHelper) {
		if (selectFiled==null||selectFiled.equals("")) {
			selectFiled = " * ";
		}
		String sql = "select "+selectFiled+" from "+getTableName();
		if (joinHelper!=null) {
			sql+=joinHelper.toString();
		}
		sql+=CheckWhereHelper(whereHelper);
		return CheckDataSourceName()?Db.use(this.dataSourceName).find(sql):Db.find(sql);
	}
	
	/**
	 * 根据条件查询到的列表，获取第一条数据
	 * 	new一个whereHelper参数：如果设置过软删除字段自动过滤
	 * null:直接不添加软删除过滤
	 * ps:看了jfinal源码，测试后发现jfinal是先取出所有数据，然后取第一条，效率不行，请参考使用数据库方言的方式
	 * @see com.loserstar.utils.db.jfinal.base.imp.BaseService.getFirstList(WhereHelper, DBType)
	 * @param whereHelper
	 * @return
	 */
	@Deprecated
	public Record getFirstList(WhereHelper whereHelper) {
		addSoftDelField(whereHelper);
		return getFirstList_notSoftDel(whereHelper);
	}
	
	/**
	 * 根据条件查询到的列表，获取第一条数据(不自动添加软删除字段过滤)
	 * ps:看了jfinal源码，测试后发现jfinal是先取出所有数据，然后取第一条，效率不行，请参考使用数据库方言的方式
	 * @see com.loserstar.utils.db.jfinal.base.imp.BaseService.getFirstList_notSoftDel(WhereHelper, DBType)
	 * @param whereHelper
	 * @return
	 */
	@Deprecated
	public Record getFirstList_notSoftDel(WhereHelper whereHelper) {
		String sql ="select * from "+getTableName()+CheckWhereHelper(whereHelper);
		return Db.findFirst(sql);
	}
	
	/**
	 * 根据条件查询到的列表，获取第一条数据
	 * 	new一个whereHelper参数：如果设置过软删除字段自动过滤
	 * null:直接不添加软删除过滤
	 * @param whereHelper
	 * @return
	 */

	public Record getFirstList(WhereHelper whereHelper,DBType dbType){
		addSoftDelField(whereHelper);
		return getFirstList_notSoftDel(whereHelper,dbType);
	}
	
	/**
	 * 根据条件查询到的列表，获取第一条数据(不自动添加软删除字段过滤)
	 * @param whereHelper
	 * @return
	 */
	public Record getFirstList_notSoftDel(WhereHelper whereHelper,DBType dbType){
		String sql ="";
		if(dbType.equals(DBType.db2)) {
			sql = "select * from "+getTableName()+CheckWhereHelper(whereHelper)+" FETCH FIRST 1 ROWS ONLY";
		}else if(dbType.equals(DBType.mysql)) {
			sql = "select * from "+getTableName()+CheckWhereHelper(whereHelper)+" LIMIT 1";
		}else if(dbType.equals(DBType.oracle)) {
			sql = "select * from "+getTableName()+CheckWhereHelper(whereHelper)+" ROWNUM <= 1";
		}else if(dbType.equals(DBType.sqlserver)) {
			sql = "select TOP number|percent column_name(s) from "+getTableName()+CheckWhereHelper(whereHelper);
		}
		return get(sql);
	}
	
	/**
	 * 查询列表(分页)的列表数据
	 * 	new一个whereHelper参数：如果设置过软删除字段自动过滤
	 * null:直接不添加软删除过滤
	 * @param pageNumber 页码
	 * @param pageSize 每页多少条
	 * @param whereHelper 查询条件
	 * @return
	 */
	public Page<Record> getListPage(int pageNumber,int pageSize,WhereHelper whereHelper){
		addSoftDelField(whereHelper);
		return getListPage_notSoftDel(pageNumber, pageSize, whereHelper);
	}
	
	/**
	 * 查询列表(分页)的列表数据(不自动添加软删除字段过滤)
	 * @param pageNumber
	 * @param pageSize
	 * @param whereHelper
	 * @return
	 */
	public Page<Record> getListPage_notSoftDel(int pageNumber,int pageSize,WhereHelper whereHelper){
		String sqlExceptSelect = "from "+getTableName()+CheckWhereHelper(whereHelper);
		return CheckDataSourceName()?Db.use(this.dataSourceName).paginate(pageNumber, pageSize, "select *",sqlExceptSelect):Db.paginate(pageNumber, pageSize, "select *",sqlExceptSelect);
	}
	
	/**
	 * 根据字符串主键id得到一条记录
	 * @param id
	 * @return
	 */
	public Record getById(String id) {
		return CheckDataSourceName()?Db.use(this.dataSourceName).findById(getTableName(),getPrimaryKey(),id):Db.findById(getTableName(), getPrimaryKey(), id);
	}
	
	/**
	 * 根据long形的主键id得到一条记录
	 * @param id
	 * @return
	 */
	public Record getById(long id) {
		return CheckDataSourceName()?Db.use(this.dataSourceName).findById(getTableName(), getPrimaryKey(), id):Db.findById(getTableName(), getPrimaryKey(), id);
	}
	
	/**
	 * 保存一条记录，根据是否有主键来决定新增还是修改(自动生成去横岗的guid)
	 * @param record
	 * @return
	 */
	public boolean save(Record record) {
		boolean flag = false;
		if (record.getStr(getPrimaryKey())==null||record.getStr(getPrimaryKey()).equals("")) {
			record.set(getPrimaryKey(), UUID.randomUUID().toString().replaceAll("-", ""));
			flag = CheckDataSourceName()?Db.use(this.dataSourceName).save(getTableName(), getPrimaryKey(), record):Db.save(getTableName(),getPrimaryKey(), record);
		}else {
			flag = CheckDataSourceName()?Db.use(this.dataSourceName).update(getTableName(),getPrimaryKey(), record):Db.update(getTableName(),getPrimaryKey(), record);
		}
		return flag;
	}
	
	/**
	 * 保存一条记录，根据是否有主键来决定新增还是修改(自动生成自增的guid，用作排序)
	 * @param record
	 * @return
	 */
	public boolean saveLongGuid(Record record) {
		boolean flag = false;
		if (record.getStr(getPrimaryKey())==null||record.getStr(getPrimaryKey()).equals("")) {
			record.set(getPrimaryKey(), SnowflakeIdWorker.FakeGuid());
			flag = CheckDataSourceName()?Db.use(this.dataSourceName).save(getTableName(),getPrimaryKey(), record):Db.save(getTableName(),getPrimaryKey(), record);
		}else {
			flag = CheckDataSourceName()?Db.use(this.dataSourceName).update(getTableName(),getPrimaryKey(), record):Db.update(getTableName(),getPrimaryKey(), record);
		}
		return flag;
	}
	
	/**
	 * 新增
	 * @param record
	 * @return
	 */
	public boolean insert(Record record) {
		return CheckDataSourceName()?Db.use(this.dataSourceName).save(getTableName(),getPrimaryKey(), record):Db.save(getTableName(),getPrimaryKey(), record);
	}
	
	/**
	 * 修改
	 * @param record
	 * @return
	 */
	public boolean update(Record record) {
		return CheckDataSourceName()?Db.use(this.dataSourceName).update(getTableName(), getPrimaryKey(),record):Db.update(getTableName(), getPrimaryKey(),record);
	}
	
	/**
	 * 删除本表的所有数据
	 * @return
	 */
	public int deleteAll() {
		return CheckDataSourceName()?Db.use(this.dataSourceName).delete("DELETE FROM "+getTableName()):Db.delete("DELETE FROM "+getTableName());
	}
	
	/**
	 * 根据条件删除数据
	 * @param whereHelper
	 * @return
	 */
	public int deleteByWhere(WhereHelper whereHelper) {
		return CheckDataSourceName()?Db.use(this.dataSourceName).delete("DELETE FROM "+getTableName()+CheckWhereHelper(whereHelper)):Db.delete("DELETE FROM "+getTableName()+CheckWhereHelper(whereHelper));
	}
	
	/**
	 * 根据主键id删除一条记录
	 * @param id
	 * @return
	 */
	public boolean deleteById(String id) {
		return CheckDataSourceName()?Db.use(this.dataSourceName).deleteById(getTableName(),getPrimaryKey(), id):Db.deleteById(getTableName(),getPrimaryKey(), id);
	}
	
	/**
	 * 根据主键软删除一条记录
	 * @param id
	 * @return
	 */
	public boolean deleteSoftById(String id) {
		boolean flag = false;
		try {
		if (getSoftDelField()==null||getSoftDelField().equals("")) {
				throw new Exception(this.getClass().getName()+"没有指定该表的软删除字段，不允许进行软删除");
		}
		Record record = new Record();
		record.set(getPrimaryKey(), id);
		record.set(getSoftDelField(), DEL);
		flag = CheckDataSourceName()?Db.use(this.dataSourceName).update(getTableName(), getPrimaryKey(), record):Db.update(getTableName(), getPrimaryKey(), record);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 批量新增
	 * @param list
	 * @return 返回每条sql影响的行数
	 */
	public int[] batchInsert(List<Record> list) {
		return CheckDataSourceName()?Db.use(this.dataSourceName).batchSave(getTableName(), list, list.size()):Db.batchSave(getTableName(), list, list.size());
	}
	
	/**
	 * 批量修改
	 * @param list
	 * @return 返回每条sql影响的行数
	 */
	public int[] batchUpdate(List<Record> list) {
		return CheckDataSourceName()?Db.use(this.dataSourceName).batchUpdate(getTableName(), list, list.size()):Db.batchUpdate(getTableName(), list, list.size());
	}
	/**
	 * 批量保存，根据flag标记来判断删除还是新增修改(c新增u修改r读取d删除)u在无id的情况下为新增
	 * @param kpiList
	 * @return
	 */
	public boolean batchCURDSaveList(List<Record> list) {
		boolean flag = true;
		 for (Record record : list) {
			 String recordFlag = record.getStr(EDIT_FLAG);
			 String id = record.getStr(getPrimaryKey());
			 if (recordFlag!=null&&recordFlag.equals("d")&&id!=null&&!id.equals("")) {
				 record.remove(EDIT_FLAG);
				flag  =deleteById(id);
			}else if(recordFlag!=null&&(recordFlag.equals("c")||recordFlag.equals("u"))) {
				record.remove(EDIT_FLAG);
				flag = saveLongGuid(record);
			}
		}
		 return flag;
	}
	
	/**
	 * 添加一个curd_flag=r的K-V到record对象中
	 * @param record
	 * @return
	 */
	public static Record setCURDFlag(Record record,String curdValue) {
		record.set(EDIT_FLAG, curdValue);
		return record;
	}
	/**
	 * 批量的添加一个curd_flag=r的K-V到record对象中
	 * @param record
	 * @return
	 */
	public static List<Record> setCURDFlag(List<Record> records,String curdValue) {
		for (Record record : records) {
			record.set(EDIT_FLAG, curdValue);
		}
		return records;
	}
	
}
