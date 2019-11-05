package com.sijing.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sijing.jpa.dao.StudentDao;
import com.sijing.jpa.entity.Student;

@Service
public class StudentService {
	
	@Autowired
	private StudentDao studentDao;
	
	/**
	 * 保存一个实体
	 * @param entity
	 * @return
	 */
	public Student save(Student entity) {
		return studentDao.save(entity);
	}

}
