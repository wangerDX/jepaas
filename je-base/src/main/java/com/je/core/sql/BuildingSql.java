package com.je.core.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.je.core.exception.PlatformException;
import com.je.core.exception.PlatformExceptionEnum;
import net.sf.json.JSONObject;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import com.je.core.constants.ConstantVars;
import com.je.core.constants.table.ColumnType;
import com.je.core.dao.PCDaoTemplateImpl;
import com.je.core.entity.extjs.DbModel;
import com.je.core.util.ArrayUtils;
import com.je.core.util.DateUtils;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;

/**
 * 用于创建可以直接执行的SQL
 * @author YUNFENGCHENG
 * 2011-8-31 上午09:33:00
 */
public class BuildingSql {
	private static Logger logger = LoggerFactory.getLogger(BuildingSql.class);
	private static BeanUtils beanUtils;
	static{
		beanUtils = BeanUtils.getInstance();
	}
	/**
	 * 实例化此类
	 * 研发部:云凤程
	 * 2011-8-31 上午09:37:45
	 * @return
	 */
	public static BuildingSql getInstance(){
		if(PCDaoTemplateImpl.DBNAME.equals(ConstantVars.STR_ORACLE)){
			return BuildingSqlHolder.BUILDING_QUERY;
		}else if(PCDaoTemplateImpl.DBNAME.equals(ConstantVars.STR_SQLSERVER)){
			return BuildingSqlHolder.BUILDING_QUERYBYSQLSERVER;
		}else if(PCDaoTemplateImpl.DBNAME.equals(ConstantVars.STR_MYSQL)){
			return BuildingSqlHolder.BUILDING_QUERYBYMYSQL;
		}else{
			return BuildingSqlHolder.BUILDING_QUERY;
		}
	}
	/**
	 * 实例化此类
	 * 研发部:云凤程
	 * 2011-8-31 上午09:37:45
	 * @return
	 */
	public static BuildingSql getInstance(String dbName){
		if(dbName.equals(ConstantVars.STR_ORACLE)){
			return BuildingSqlHolder.BUILDING_QUERY;
		}else if(dbName.equals(ConstantVars.STR_SQLSERVER)){
			return BuildingSqlHolder.BUILDING_QUERYBYSQLSERVER;
		}else if(dbName.equals(ConstantVars.STR_MYSQL)){
			return BuildingSqlHolder.BUILDING_QUERYBYMYSQL;
		}else{
			return BuildingSqlHolder.BUILDING_QUERY;
		}
	}
	/**
	 * 静态内部类用于初始化对象和缓存树形
	 * 研发部:云凤程
	 * 2011-8-31 上午09:38:00
	 */
	private static class BuildingSqlHolder{
		private static final BuildingSqlBySQLServer BUILDING_QUERYBYSQLSERVER=new BuildingSqlBySQLServer();
		private static final BuildingSqlByMySQL BUILDING_QUERYBYMYSQL=new BuildingSqlByMySQL();
		private static final BuildingSql BUILDING_QUERY = new BuildingSql();
	}
	/**
	 * 辅助函数,生成添加列的SQL
	 * @param column
	 * @return
	 */
	private String getDDL4AddColumns(DynaBean column,List<String> notNullCodes){
		StringBuilder builder = new StringBuilder();
		String type=column.getStr("TABLECOLUMN_TYPE");
		String code=column.getStr("TABLECOLUMN_CODE");
		String isNull=column.getStr("TABLECOLUMN_ISNULL");
		String length=column.getStr("TABLECOLUMN_LENGTH");
		if(notNullCodes!=null && !"1".equals(isNull) && notNullCodes.contains(code)){
			isNull="1";
		}
		if(ColumnType.ID.equals(type)){
			//id
			builder.append(code+" VARCHAR2(50) not null,\n");
		}else if(ColumnType.CLOB.equals(type)){
			//大数据
			builder.append(code+" CLOB");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.BIGCLOB.equals(type)){
			//大数据
			builder.append(code+" CLOB");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.DATE.equals(type)){
			//时间
			builder.append(code+" VARCHAR2(12)");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.DATETIME.equals(type)){
			//时间
			builder.append(code+" VARCHAR2(20)");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.FLOAT.equals(type)){
			//小数
			String numVal="";
			if(StringUtil.isNotEmpty(length)){
				if(length.indexOf(",")!=-1){
					numVal=length.replace(",", "@");
				}else{
					numVal="20@"+length;
				}
			}else{
				numVal="20@2";
			}
//			length=StringUtil.getDefaultValue(length,"2");
			builder.append(code+" NUMBER("+numVal+")");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.FLOAT2.equals(type)){
			//小数
			length="2";
			builder.append(code+" NUMBER(20@"+length+")");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.CUSTOM.equals(type)){
			//自定义,当选择自定义的时候长度 就写成类似(VARCHAE2(154))
			length=length.replaceAll(",", "@");
			builder.append(code+" "+length);
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.NUMBER.equals(type)){
			//整数
			length=StringUtil.getDefaultValue(length,"20");
			builder.append(code+" NUMBER("+length+")");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.VARCHAR1000.equals(type)){
			//大字符串
			builder.append(code+" VARCHAR2(1000)");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.VARCHAR4000.equals(type)){
			//大字符串
			builder.append(code+" VARCHAR2(4000)");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.VARCHAR255.equals(type)){
			//一般字符串
			builder.append(code+" VARCHAR2(255)");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.VARCHAR100.equals(type)){
			//一般字符串
			builder.append(code+" VARCHAR2(100)");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.VARCHAR50.equals(type)){
			//一般字符串
			builder.append(code+" VARCHAR2(50)");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.VARCHAR30.equals(type)){
			//一般字符串
			builder.append(code+" VARCHAR2(30)");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.VARCHAR.equals(type)){
			//自定义长度字符串
			length=StringUtil.getDefaultValue(length,"255");
			if("MAX".equals(length) || "5000".equals(length)){
				length="4000";
			}
			builder.append(code+" VARCHAR2("+length+")");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.YESORNO.equals(type)){
			//YESORNO
			builder.append(code+" VARCHAR2(4)");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else if(ColumnType.FOREIGNKEY.equals(type)){
			builder.append(code+" VARCHAR2(50)");
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",");
		}else if(StringUtil.isNotEmpty(type)){
			builder.append(code+" "+type);
			if(!"1".equals(isNull)){
				builder.append(" not null");
			}
			builder.append(",\n");
		}else{
			throw new PlatformException("数据表格SQL构建异常", PlatformExceptionEnum.JE_CORE_TABLE_BUILDING_ERROR,new Object[]{column,notNullCodes});
		}
		return builder.toString();
	}
	/**
	 * 辅助函数 生成创建键的DDL语句
	 * @param key
	 * @param resourceTable
	 * @return
	 */
	private void getDDL4AddKey(DynaBean key,DynaBean resourceTable,List<String> lists){
		StringBuilder builder = new StringBuilder();
		if("Primary".equals(key.getStr("TABLEKEY_TYPE"))){
			builder.append("alter table "+resourceTable.get("RESOURCETABLE_TABLECODE")+"\n "+
					"add primary key ("+key.get("TABLEKEY_COLUMNCODE")+")\n "+
					"using index \n "+
					"pctfree 10 \n "+
					"initrans 2 \n "+
					"maxtrans 255 \n "+
					"storage( \n "+
					"initial 64K \n "+
					"minextents 1 \n "+
					"maxextents unlimited \n"+
					")"
			);
			lists.add(builder.toString());
		}else if("Unique".equals(key.getStr("TABLEKEY_TYPE"))){
			builder.append("alter table "+resourceTable.get("RESOURCETABLE_TABLECODE")+" \n"+
					"add constraint "+key.getStr("TABLEKEY_CODE").replaceAll("-", "")+" unique ("+key.get("TABLEKEY_COLUMNCODE")+") \n"+
					"using index  \n"+
					"pctfree 10 \n"+
					"initrans 2 \n"+
					"maxtrans 255 \n"+
					"storage( \n"+
					"initial 64K \n"+
					"minextents 1 \n"+
					"maxextents unlimited \n"+
					")"
			);
			lists.add(builder.toString());
		}else if("Foreign".equals(key.getStr("TABLEKEY_TYPE")) || "Inline".equals(key.getStr("TABLEKEY_TYPE"))){
			StringBuilder ddl = new StringBuilder("alter table "+resourceTable.get("RESOURCETABLE_TABLECODE")+" \n");
			ddl.append("add constraint "+key.getStr("TABLEKEY_CODE")+" foreign key ("+key.get("TABLEKEY_COLUMNCODE")+") \n");
			if("Cascade".equals(key.getStr("TABLEKEY_LINETYLE"))){
				ddl.append("references "+key.getStr("TABLEKEY_LINKTABLE")+" ("+key.getStr("TABLEKEY_LINECOLUMNCODE")+") on delete cascade");
			}else if("Noaction".equals(key.getStr("TABLEKEY_LINETYLE"))){
				ddl.append("references "+key.getStr("TABLEKEY_LINKTABLE")+" ("+key.getStr("TABLEKEY_LINECOLUMNCODE")+")");
			}else if("Setnull".equals(key.getStr("TABLEKEY_LINETYLE"))){
				ddl.append("references "+key.getStr("TABLEKEY_LINKTABLE")+" ("+key.getStr("TABLEKEY_LINECOLUMNCODE")+") on delete set null");
			}
			builder.append(ddl.toString());
			lists.add(builder.toString());
			if("0".equals(key.getStr("TABLEKEY_ISRESTRAINT"))){
				lists.add(" ALTER TABLE "+resourceTable.get("RESOURCETABLE_TABLECODE")+" DISABLE CONSTRAINT "+key.getStr("TABLEKEY_CODE").replaceAll("-", "")+"");
			}
		}
	}
	/**
	 * 辅助函数 生成创建索引的DDL语句
	 * @param index
	 * @param resourceTable
	 * @return
	 */
	public String getDDL4AddIndex(DynaBean index,DynaBean resourceTable){
		StringBuilder builder = new StringBuilder();
		if(index.getStr("TABLEINDEX_FIELDCODE").equals(resourceTable.getStr("RESOURCETABLE_PKCODE"))){
			return "";
		}
		builder.append("create");
		if("1".equals(index.getStr("TABLEINDEX_UNIQUE"))){
			builder.append(" unique index ");
		}else{
			builder.append(" index ");
		}
		builder.append(index.getStr("TABLEINDEX_NAME")+" on ");
		builder.append(resourceTable.getStr("RESOURCETABLE_TABLECODE")+"("+index.getStr("TABLEINDEX_FIELDCODE")+")");
		return builder.toString();
	}
	/**
	 * 辅助函数 生成创建键的DDL语句
	 * @param uniqueCode
	 * @param columnCode
	 * @return
	 */
	private String getDDL4AddUnqiue(String uniqueCode,String columnCode,String tableCode){
		StringBuffer ddlSql=new StringBuffer();
		ddlSql.append("alter table "+tableCode+" \n"+
				"add constraint "+uniqueCode+" unique ("+columnCode+") \n"+
				"using index  \n"+
				"pctfree 10 \n"+
				"initrans 2 \n"+
				"maxtrans 255 \n"+
				"storage( \n"+
				"initial 64K \n"+
				"minextents 1 \n"+
				"maxextents unlimited \n"+
				")"
		);
		return ddlSql.toString();
	}
	/**
	 * 得到创建表的DDL语言
	 * @param resourceTable
	 * @return
	 */
	public List<String> getDDL4CreateTable(DynaBean resourceTable){
		List<String> list = new ArrayList<String>();
		StringBuilder createSql = new StringBuilder("create table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" (\n");
		List<DynaBean> columns = (List<DynaBean>) resourceTable.get(BeanUtils.KEY_TABLE_COLUMNS);
		String pkCode="";
		//唯一性索引字段
		List<String> uniqueColumns=new ArrayList<String>();
		for(DynaBean column : columns){
			//提取到外面
			createSql.append(getDDL4AddColumns(column,null).replace("@", ","));
			//维护唯一性索引
			if(ColumnType.ID.equals(column.getStr("TABLECOLUMN_TYPE"))){
				pkCode=column.getStr("TABLECOLUMN_CODE");
				continue;
			}
			if("1".equals(column.getStr("TABLECOLUMN_UNIQUE")) && !ColumnType.ID.equals(column.getStr("TABLECOLUMN_TYPE"))  && !column.getStr("TABLECOLUMN_CODE","").equals(resourceTable.getStr("RESOURCETABLE_PKCODE"))){
				uniqueColumns.add(column.getStr("TABLECOLUMN_CODE"));
			}
		}
		//1.生成创建表的基本DDL语句
		String sql = createSql.substring(0,createSql.length()-2)+"\n)";
		list.add(sql);
		//2.生成修改表的注释
		list.add("comment on table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" is '"+resourceTable.getStr("RESOURCETABLE_TABLENAME")+"'");
		for(DynaBean column : columns){
			list.add("comment on column "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+"."+column.get("TABLECOLUMN_CODE")+" is '"+column.get("TABLECOLUMN_NAME")+"'");
		}
		//2.生成表中存在的键
		List<DynaBean> keys = (List<DynaBean>) resourceTable.get(BeanUtils.KEY_TABLE_KEYS);
		for(DynaBean key : keys){
			getDDL4AddKey(key, resourceTable,list);
		}
		//3.维护唯一性约束
		for(String columnCode:uniqueColumns){
			String uniqueCode="JE_UNIQUE_"+DateUtils.getUniqueTime();
			list.add(getDDL4AddUnqiue(uniqueCode,columnCode, resourceTable.getStr("RESOURCETABLE_TABLECODE")));
			list.add("UPDATE JE_CORE_TABLECOLUMN SET TABLECOLUMN_UNIQUECODE='"+uniqueCode+"' WHERE TABLECOLUMN_CODE='"+columnCode+"' AND TABLECOLUMN_RESOURCETABLE_ID='"+resourceTable.getStr("JE_CORE_RESOURCETABLE_ID")+"'");
		}
		//4.加入索引
		List<DynaBean> indexs = (List<DynaBean>) resourceTable.get(BeanUtils.KEY_TABLE_INDEXS);
		for(DynaBean index : indexs){
			if(pkCode.equals(index.getStr("TABLEINDEX_FIELDCODE"))){
				continue;
			}
			list.add(getDDL4AddIndex(index, resourceTable));
		}
		return list;
	}
	/**
	 * 得到更新表的DDL语言
	 * @param resourceTable
	 * @return
	 */
	public List<String> getDDL4UpdateTable(DynaBean resourceTable,List<DbModel> dbFields){
		List<String> clobFields=new ArrayList<String>();
		List<String> fieldCodes=new ArrayList<String>();
		List<String> notNullCodes=new ArrayList<String>();
		for(DbModel dbModel:dbFields){
			fieldCodes.add(dbModel.getCode());
			if(ColumnType.CLOB.equalsIgnoreCase(dbModel.getType())){
				clobFields.add(dbModel.getCode());
			}
			if(!dbModel.isNull()){
				notNullCodes.add(dbModel.getCode());
			}
		}
		List<String> arraySql = new ArrayList<String>();
		String oldTableCode=resourceTable.getStr("RESOURCETABLE_OLDTABLECODE");
		if(StringUtil.isNotEmpty(oldTableCode) && !oldTableCode.equals(resourceTable.getStr("RESOURCETABLE_TABLECODE"))){
			arraySql.add("alter table "+resourceTable.getStr("RESOURCETABLE_OLDTABLECODE")+" rename to "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+";");
		}
		//1.修改表的注释
		arraySql.add("comment on table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" is '"+resourceTable.getStr("RESOURCETABLE_TABLENAME")+"'");
		//2.修改列信息
		List<DynaBean> columns = (List<DynaBean>) resourceTable.get(BeanUtils.KEY_TABLE_COLUMNS);
		//需要删除的唯一性约束的字段
		List<String> delUniqueColumns=new ArrayList<String>();
		//需要增加的唯一性约束的字段
		List<String> addUniqueColumns=new ArrayList<String>();
		//获取原数据库的数据类型  主要处理clob字段

		//获取数据库的表字段信息
		for(DynaBean column : columns){
			boolean update=false;
			if("1".equals(column.getStr("TABLECOLUMN_ISCREATE")) && (fieldCodes.contains(column.getStr("TABLECOLUMN_CODE","").toUpperCase()) ||  (fieldCodes.contains(column.getStr("TABLECOLUMN_OLDCODE","").toUpperCase()))
					&& !column.getStr("TABLECOLUMN_CODE","").toUpperCase().equals(column.getStr("TABLECOLUMN_OLDCODE","").toUpperCase()))){
				update=true;
			}else if(fieldCodes.contains(column.getStr("TABLECOLUMN_CODE","").toUpperCase())){
				update=true;
				column.setStr("TABLECOLUMN_OLDCODE",column.getStr("TABLECOLUMN_CODE"));
			}else{
				update=false;
			}

			if(update){
				//判断是否更改了列名   (不区分大小写)
				if(!column.getStr("TABLECOLUMN_CODE").equalsIgnoreCase(column.getStr("TABLECOLUMN_OLDCODE"))){
					arraySql.add("alter table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" rename column "+column.getStr("TABLECOLUMN_OLDCODE")+" to "+column.getStr("TABLECOLUMN_CODE"));
				}
				//更新 就更新长度和 注释   //主键不修改
				if(!column.getStr("TABLECOLUMN_CODE").equals(resourceTable.get("RESOURCETABLE_PKCODE"))){
					//增加clob的特殊处理
					String columnType=column.getStr("TABLECOLUMN_TYPE");
					String columnCode=column.getStr("TABLECOLUMN_CODE","").toUpperCase();
					String pre="1";
					if(ColumnType.CLOB.equals(columnType) || ColumnType.BIGCLOB.equals(columnType)){
						if(clobFields.contains(columnCode)){
							//不加入更新
						}else{
							//加入更新   将其他类型更新程大文本
							arraySql.add("alter table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" rename column "+column.getStr("TABLECOLUMN_CODE")+" to "+column.getStr("TABLECOLUMN_CODE")+pre);
							arraySql.add("alter table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" add "+getDDL4AddColumns(column,null).replaceAll(",", "").replace("@", ","));
							arraySql.add("UPDATE "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" SET "+column.getStr("TABLECOLUMN_CODE")+"="+column.getStr("TABLECOLUMN_CODE")+pre);
							arraySql.add("alter table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" drop COLUMN "+column.getStr("TABLECOLUMN_CODE")+pre+" ");
						}
					}else if(clobFields.contains(columnCode) || checkNumber(dbFields,column)){
						arraySql.add("alter table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" rename column "+column.getStr("TABLECOLUMN_CODE")+" to "+column.getStr("TABLECOLUMN_CODE")+pre);
						arraySql.add("alter table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" add "+getDDL4AddColumns(column,null).replaceAll(",", "").replace("@", ","));
						arraySql.add("UPDATE "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" SET "+column.getStr("TABLECOLUMN_CODE")+"="+column.getStr("TABLECOLUMN_CODE")+pre);
						arraySql.add("alter table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" drop COLUMN "+column.getStr("TABLECOLUMN_CODE")+pre+" ");
					}else{
						arraySql.add("alter table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" modify "+getDDL4AddColumns(column,notNullCodes).replaceAll(",", "").replace("@", ","));
					}
				}
				//维护唯一性索引
				if(resourceTable.get("RESOURCETABLE_PKCODE").equals(column.getStr("TABLECOLUMN_CODE"))){
					continue;
				}
				String unique=column.getStr("TABLECOLUMN_UNIQUE");
				String oldUnique=column.getStr("TABLECOLUMN_OLDUNIQUE");
				if(!ColumnType.ID.equals(column.getStr("TABLECOLUMN_TYPE"))){
					//添加唯一性约束
					if("1".equals(unique) && "0".equals(oldUnique)){
						addUniqueColumns.add(column.getStr("TABLECOLUMN_CODE"));
					}else if("0".equals(unique) && "1".equals(oldUnique)){
						delUniqueColumns.add(column.getStr("TABLECOLUMN_UNIQUECODE"));
					}
				}
			}else{
				//添加
				arraySql.add("alter table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" add "+getDDL4AddColumns(column,null).replaceAll(",", "").replace("@", ","));
				if("1".equals(column.getStr("TABLECOLUMN_UNIQUE")) && !ColumnType.ID.equals(column.getStr("TABLECOLUMN_TYPE"))){
					addUniqueColumns.add(column.getStr("TABLECOLUMN_CODE"));
				}
			}
			arraySql.add("comment on column "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+"."+column.getStr("TABLECOLUMN_CODE")+" is '"+column.getStr("TABLECOLUMN_NAME")+"'");
		}
		//3.修改键信息
		List<DynaBean> keys = (List<DynaBean>) resourceTable.get(BeanUtils.KEY_TABLE_KEYS);
		for(DynaBean key : keys){
			if("1".equals(key.getStr("TABLEKEY_ISCREATE"))){
				//更新
				//暂时不能更新,只能添加
			}else{
				//添加
				getDDL4AddKey(key, resourceTable,arraySql);
			}
		}
		//4.加入索引
		List<DynaBean> indexs = (List<DynaBean>) resourceTable.get(BeanUtils.KEY_TABLE_INDEXS);
		for(DynaBean index : indexs){
			if("1".equals(index.getStr("TABLEINDEX_ISCREATE"))){
				//不支持更新索引
			}else{
				arraySql.add(getDDL4AddIndex(index, resourceTable));
			}
		}
		//5.维护唯一性约束
		for(String columnCode:addUniqueColumns){
			String uniqueCode="JE_UNIQUE_"+DateUtils.getUniqueTime();
			arraySql.add(getDDL4AddUnqiue(uniqueCode,columnCode, resourceTable.getStr("RESOURCETABLE_TABLECODE")));
			arraySql.add("UPDATE JE_CORE_TABLECOLUMN SET TABLECOLUMN_UNIQUECODE='"+uniqueCode+"' WHERE TABLECOLUMN_CODE='"+columnCode+"' AND TABLECOLUMN_RESOURCETABLE_ID='"+resourceTable.getStr("JE_CORE_RESOURCETABLE_ID")+"'");
		}
		for(String uniqueCode:delUniqueColumns){
			arraySql.add(" alter table "+resourceTable.getStr("RESOURCETABLE_TABLECODE")+" DROP constraint "+uniqueCode+" ");
		}

		return arraySql;
	}
	/**
	 * 检测数值类型是否减少了精度
	 * @param dbModels
	 * @param column
	 * @return
	 */
	private Boolean checkNumber(List<DbModel> dbModels,DynaBean column) {
		if(ColumnType.NUMBER.equals(column.getStr("TABLECOLUMN_TYPE"))) {
			Integer length=Integer.parseInt(StringUtil.getDefaultValue(column.getStr("TABLECOLUMN_LENGTH"),"20"));
			for(DbModel dbModel:dbModels) {
				if(dbModel.getCode().equals(column.getStr("TABLECOLUMN_CODE"))) {
					Integer dbLength=20;
					if(StringUtil.isNotEmpty(dbModel.getLength())) {
						dbLength=Integer.parseInt(dbModel.getLength());
					}
					if(dbLength>length) {
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * 得到用于excel导出功能的sql ,主要工作是吧ddcode格式化成ddName
	 * @param tableName
	 * @param whereSql
	 * @param fieldsCode
	 * @param whereSql
	 * @param fieldDD
	 * @return
	 */
	public String getSql4Exp(String tableName, String fieldsCode,String fieldDD, String whereSql,String orderSql){
		JSONObject jb = JSONObject.fromObject(fieldDD);
		StringBuffer sql = new StringBuffer(
				"select "+
						assistGetSql4Exp(fieldsCode.split(ArrayUtils.SPLIT), jb)+
						" from "+tableName+" PCAT2"+
						" where 1=1 "+whereSql+" "+orderSql
		);

		return sql.toString();
	}
	/**
	 * 辅助函数
	 * @return
	 */
	private String assistGetSql4Exp(String[] fieldsCode,JSONObject jb){
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < fieldsCode.length; i++) {
			if(jb.get(fieldsCode[i]) == null){//不用字典
				str.append(fieldsCode[i]+ArrayUtils.SPLIT);
			}else{//用到字典(只支持内部字典)
				str.append("(select DICTIONARYITEM_ITEMNAME from JE_CORE_DICTIONARY p, JE_CORE_DICTIONARYITEM c where "+
						"p.DICTIONARY_DDCODE = '"+jb.get(fieldsCode[i])+
						"' and p.JE_CORE_DICTIONARY_ID = c.DICTIONARYITEM_DICTIONARY_ID and c.DICTIONARYITEM_ITEMCODE = PCAT2."+fieldsCode[i]+") "+fieldsCode[i]+",");
			}
		}
		String over = str.substring(0, str.length()-1);
		return over;
	}
	/**
	 * 研发部:云凤程
	 * 生成数据库的查询操作(全部是通过预处理的)
	 * @param dynaBean
	 * @return
	 */
	public String getInsertSql(DynaBean dynaBean){
		//1.得到表结构的描述信息
		String tableCode = dynaBean.getStr(BeanUtils.KEY_TABLE_CODE);
		DynaBean table = beanUtils.getResourceTable(tableCode);
		//2.拼装预处理的SQL
		StringBuilder insertSql = new StringBuilder();
		String fieldNames = beanUtils.getNames4Sql(table);//表字典集合用","分开
		String values = beanUtils.getValues4Sql(table);//表字典集合用 格式化成 :name,:age
		insertSql.append("INSERT INTO "+tableCode+"("+fieldNames+") VALUES ("+values+")");
		return insertSql.toString();
	}
	/**
	 * 研发部:云凤程
	 * 生成根据ID或者where生成更新BEAN的SQL
	 * @param useWhere 是否用whereh
	 * @param dynaBean
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getUpdateByIdOrWhereSql(DynaBean dynaBean,boolean useWhere){
		//1.得到表结构
		String tabelCode = dynaBean.getStr(BeanUtils.KEY_TABLE_CODE);
		DynaBean table = beanUtils.getResourceTable(tabelCode);
		//2.得到列模式
//		List<DynaBean> columns = (List<DynaBean>) table.get(BeanUtils.KEY_TABLE_COLUMNS);
		//3.得到表的主键编码
		String tablePKCode = dynaBean.getStr(BeanUtils.KEY_PK_CODE);
		//4.得到表主键的值
		String tablePKValue = dynaBean.getStr(tablePKCode);
		String where = dynaBean.getStr(BeanUtils.KEY_WHERE);
		//5.拼接SQL
		StringBuilder updateSql = new StringBuilder("UPDATE "+tabelCode+" SET ");
		Map<String, Object> map = dynaBean.getValues();
		//获取执行修改值的修改语句
		String updateInfos = beanUtils.getUpdateInfos4Sql(table,map);//表字典集合用","分开
		updateSql.append(updateInfos);
		if(useWhere){
			if(StringUtil.isNotEmpty(where)) {
				updateSql.append(" WHERE 1=1 "+where);
			}
		}else{
			updateSql.append(" WHERE "+tablePKCode+" =:"+tablePKCode+" ");
		}
		return updateSql.toString();
	}
	/**
	 * 得到Bean修改的sql语句 (主要用于表格列表更新)
	 * @param tableCode
	 * @param pkName
	 * @param changes
	 * @return
	 */
	public String getUpdateSql(String tableCode,String pkName,Map changes){
		StringBuffer updateSql=new StringBuffer();
		updateSql.append("UPDATE "+tableCode+" SET ");
		String pkValue="";
		for(Object obj:changes.entrySet()){
			Entry entry=(Entry) obj;
			String k=StringUtil.getDefaultValue(entry.getKey(), "");
			if(pkName.equals(k)){
				pkValue=StringUtil.getDefaultValue(entry.getValue(), "");
				continue;
			}
			String v=null;
			if(entry.getValue()!=null){
				v=StringUtil.getDefaultValue(entry.getValue(), "");
			}
			if(v==null){
				updateSql.append(k+"=NULL,");
			}else{
				updateSql.append(k+"='"+v+"',");
			}
		}
		updateSql.deleteCharAt(updateSql.length()-1);
		updateSql.append(" WHERE "+pkName+"='"+pkValue+"'");
		return updateSql.toString();
	}
	/**
	 * 研发部:云凤程
	 * 生成数据库的根据ID删除的SQl
	 * @param dynaBean
	 * @return
	 */
	public String getDeleteByIdSql(DynaBean dynaBean){
		//1.得到表结构的描述信息
		String tableCode = dynaBean.getStr(BeanUtils.KEY_TABLE_CODE);
		//2.得到表的主键编码
		String tablePKCode = dynaBean.getStr(BeanUtils.KEY_PK_CODE);
		//3.得到表主键的值
		String tablePKValue = dynaBean.getStr(tablePKCode);
		if(tablePKValue == null || "".equals(tablePKValue)){
			throw new PlatformException("拼装删除语句保存id不能使空或者是空...",PlatformExceptionEnum.JE_CORE_DYNABEAN_ERROR);
		}
		//4.拼SQL
		String deleteSql = "DELETE FROM "+tableCode+" WHERE "+tablePKCode+" = ?";
		return deleteSql;
	}
	/**
	 * 研发部:云凤程
	 * 生成数据根据where删除的SQL
	 * @param dynaBean
	 * @return
	 */
	public String getDeleteByWhereSql(DynaBean dynaBean){
		//1.得到表结构的描述信息
		String tableCode = dynaBean.getStr(BeanUtils.KEY_TABLE_CODE);
		//2.得到删除的条件
		String whereSql = dynaBean.getStr(BeanUtils.KEY_WHERE);
		//3.拼接SQL 注意:where 条件一定要用 and 开始
		String deleteSql = "DELETE FROM "+tableCode+" WHERE 1=1 "+whereSql;
		return deleteSql;
	}
	/**
	 * 研发部:云凤程
	 * 生成数据根据where删除的SQL
	 * @param ids 用,分开的主键id串
	 * @param tableName 表名称
	 * @param idName 表主键名字
	 * @return
	 */
	public String getDeleteByIdsSql(String ids ,String tableName ,String idName){
		//1.格式化好sql中用于in操作的id字符串
//		String idValues = "'"+ids.replaceAll(",", "','")+"'";
		Integer paramCount=0;
		for(String id:ids.split(",")){
			if(StringUtil.isNotEmpty(id)){
				paramCount++;
			}
		}
		String idValues=StringUtil.buildSplitString(StringUtil.getSameString("?",paramCount).split(""),",");
		if(paramCount<=0){
			idValues="''";
		}
		//2.拼接SQL
		String deleteSql = "DELETE FROM "+tableName + " WHERE "+idName+" in ("+idValues+")";
		return deleteSql;
	}
	/**
	 * 研发部:云凤程
	 * 生成根据id查询一条数据的SQL
	 * @param dynaBean
	 * @return
	 */
	public String getSelecOnetByIdSql(DynaBean dynaBean){
		//1.得到表结构的描述信息
		String tableCode = dynaBean.getStr(BeanUtils.KEY_TABLE_CODE);
		//2.得到表的主键编码
		String tablePKCode = dynaBean.getStr(BeanUtils.KEY_PK_CODE);
		//4.得到要查询的列
		String tableFileds = dynaBean.getStr(BeanUtils.KEY_QUERY_FIELDS,"");
		if(StringUtil.isEmpty(tableFileds)){
			tableFileds=dynaBean.getStr(BeanUtils.DEF_ALL_FIELDS);
		}
		//5.拼接SQl
		String seletcSql = "SELECT "+tableFileds+" FROM "+tableCode+" WHERE "+tablePKCode+" = ?";
		return seletcSql;
	}
	/**
	 * 研发部:云凤程
	 * 生成根据WHERE查询一条数据的SQL
	 * @param dynaBean
	 * @return
	 */
	public String getSelectOneByWhereSql(DynaBean dynaBean){
		//1.得到表结构的描述信息
		String tableCode = dynaBean.getStr(BeanUtils.KEY_TABLE_CODE);
		//2.得到查询条件
		String where = dynaBean.getStr(BeanUtils.KEY_WHERE);
		//3.得到要查询的列
		String tableFileds = dynaBean.getStr(BeanUtils.KEY_QUERY_FIELDS,"");
		if(StringUtil.isEmpty(tableFileds)){
			tableFileds=dynaBean.getStr(BeanUtils.DEF_ALL_FIELDS);
		}
		//4.拼接SQl
		String seletcSql = "SELECT "+tableFileds+" FROM "+tableCode+" WHERE 1=1 "+where;
		if(dynaBean.get(BeanUtils.KEY_ORDER) != null && !"".equals(dynaBean.get(BeanUtils.KEY_ORDER))){
			seletcSql = seletcSql + StringUtil.BLANK_SPACE + dynaBean.get(BeanUtils.KEY_ORDER);
		}
		return seletcSql;
	}
	/**
	 * 研发部:云凤程
	 * 生成根据WHERE查询数据的记录数
	 * @param dynaBean
	 * @return
	 */
	public String getSelectCountByWhereSql(DynaBean dynaBean){
		//1.得到表结构的描述信息
		String tableCode = dynaBean.getStr(BeanUtils.KEY_TABLE_CODE);
		//2.得到查询条件
		String where = dynaBean.getStr(BeanUtils.KEY_WHERE);
		//3.拼接SQl
		String seletcSql = "SELECT COUNT(*) FROM "+tableCode+" WHERE 1=1 "+where;
		return seletcSql;
	}
	/**
	 * 删除列的ddl语句
	 * @param tableCode
	 * @param columns
	 * @return
	 */
	public List<String> getDeleteColumnSql(String tableCode,List<DynaBean> columns){
		List<String> delSqls=new ArrayList<String>();
		for(DynaBean tc:columns){
			if("1".equals(tc.getStr("TABLECOLUMN_UNIQUE")) && "1".equals(tc.getStr("TABLECOLUMN_OLDUNIQUE"))){
				delSqls.add(" alter table "+tableCode+" DROP constraint "+tc.getStr("TABLECOLUMN_UNIQUECODE")+" ");
			}
			if(StringUtil.isNotEmpty(tc.getStr("TABLECOLUMN_CODE")) && "1".equals(tc.getStr("TABLECOLUMN_ISCREATE"))){
				delSqls.add("alter table "+tableCode+" drop COLUMN "+tc.getStr("TABLECOLUMN_CODE")+" ");
			}
		}
		return delSqls;
	}
	/**
	 * 删除键的ddl语句
	 * @param tableCode
	 * @param keys
	 * @return
	 */
	public String getDeleteKeySql(String tableCode,List<DynaBean> keys){
		StringBuffer delSql=new StringBuffer();
		for(DynaBean key:keys){
			if(StringUtil.isNotEmpty(key.getStr("TABLEKEY_CODE")) && "1".equals(key.getStr("TABLEKEY_ISCREATE"))){
				delSql.append(" alter table "+tableCode+" DROP constraint "+key.getStr("TABLEKEY_CODE")+"\n");
			}
		}
		return delSql.toString();
	}
	/**
	 * 删除索引的ddl语句
	 * @param tableCode
	 * @param indexs
	 * @return
	 */
	public String getDeleteIndexSql(String tableCode,List<DynaBean> indexs){
		StringBuffer delSql=new StringBuffer();
		for(DynaBean index:indexs){
			if(StringUtil.isNotEmpty(index.getStr("TABLEINDEX_NAME")) && "1".equals(index.getStr("TABLEINDEX_ISCREATE"))){
				delSql.append("drop index "+index.getStr("TABLEINDEX_NAME")+"");
			}
		}
		return delSql.toString();
	}
}
