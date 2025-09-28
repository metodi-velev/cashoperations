package com.example.cashdocumentsservice.repository;

import com.example.cashdocumentsservice.model.MyFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyFileRepository extends JpaRepository<MyFile, Long> {

    List<MyFile> findByFileGroup(String fileGroup);

    MyFile findByFileGroupAndFileName(String fileGroup, String fileName);
}
