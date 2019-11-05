package com.sijing.jpa.dao;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sijing.jpa.entity.Student;

public interface StudentDao extends JpaRepository<Student, Integer> {
	
	// AND
	Student findByNameAndPhone(String name,Integer phone);
	
	// OR
	Student findByNameOrPhone(String name,Integer phone);

	// IS Equals
	Student findByName(String name);
	Student findByNameIs(String name);
	Student findByNameEquals(String name);
	
	// Between = where age between ?1 and ?2
	Student findByAgeBetween(Integer minAge,Integer maxAge);
	
	// LessThan =  where age < ?1
	Student findByAgeLessThan(Integer age);
	
	// LessThanEqual = where age <= ?1 
	Student findByAgeLessThanEqual(Integer age);

	// GreaterThan =  where age > ?1
	Student findByAgeGreaterThan(Integer age);
	
	// GreaterThanEqual = where age >= ?1 
	Student findByAgeGreaterThanEqual(Integer age);
	
	// After = where create_time > ?1
	Student findByCreateTimeAfter(Date time);
	
	// Before = where create_time < ?1
	Student findByCreateTimeBefore(Date time);
	
	// IsNull = where age is null
	Student findByAgeIsNull();
	
	// IsNotNull NotNull = where age is not null
	Student findByAgeIsNotNull();
	Student findByAgeNotNull();
	
	// Like = where name like ?1 (TIPS:name 前后应加 %)
	Student findByNameLike(String name);
	
	// NotLike = where name not like ?1 (TIPS:name 前后应加 %)
	Student findByNameNotLike(String name);
	
	// StartingWith = where name like %?1
	Student findByNameStartingWith(String name);
	
	// EndingWith = where name like ?1%
	Student findByNameEndingWith(String name);
	
	// Containing = where name like %?1%
	Student findByNameContaining(String name);
	
	// OrderBy = where age = ?1 order by age desc
	Student findByAgeOrderByAgeDesc(Integer age);
	
	// Not = where name <> ?1
	Student findByNameNot(String name);
	
	// In = where age in ?1
	Student findByAgeIn();
	
	// NotIn  = where age not in ?1
	Student findByAgeNotIn();
	
	// True = where enabled = true
	Student findByEnabledTrue();
	
	// False
	Student findByEnabledFalse();
	
	// IgnoreCase = where UPPER(name) = UPPER(?1)
	Student findByNameIgnoreCase(String name);
}


