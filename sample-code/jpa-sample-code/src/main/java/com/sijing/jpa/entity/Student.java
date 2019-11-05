package com.sijing.jpa.entity;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

// 定义对象未JPA管理的实体
@Entity
// 指定数据库表名
@Table(name = "student")
public class Student {

	// 定义为数据库主键
	@Id
	// 主键生成策略 具有四种策略
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Integer id;

	private String name;

	private Integer sex;

	private Integer age;

	private Integer phone;
	
	private Boolean enabled; 

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	// 定义属性名称于数据库字段名的映射
	@Column(name = "cerate_time")
	private Timestamp createTime;

	// 不对该字段进行映射
	@Transient
	// 将Date类型映射到对应精度的字段
	//	@Temporal(TemporalType.DATE) 只有日期
	//	@Temporal(TemporalType.TIME) 只有时间
	@Temporal(TemporalType.TIMESTAMP) // 日期 + 时间
	private Date updateTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Integer getPhone() {
		return phone;
	}

	public void setPhone(Integer phone) {
		this.phone = phone;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}
