package com.lime.batchwithdatabase.repository;

import com.lime.batchwithdatabase.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyPersonRepository extends JpaRepository<Person, Integer> {
}
