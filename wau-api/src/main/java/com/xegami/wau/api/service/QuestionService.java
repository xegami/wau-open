package com.xegami.wau.api.service;

import com.xegami.wau.api.domain.Question;
import com.xegami.wau.api.dto.QuestionDTO;
import com.xegami.wau.api.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class QuestionService extends BaseService<Question, QuestionDTO> {

    private static final String QUESTIONS_PATH = "C:\\prod\\wau\\questions.xlsx";

    @Autowired
    private QuestionRepository questionRepository;

    public QuestionService() {
        super(Question.class, QuestionDTO.class);
    }

    @Override
    protected JpaRepository<Question, Long> setRepository() {
        return questionRepository;
    }

    @Transactional
    public void loadQuestions() throws IOException {
        questionRepository.deleteAll();

        FileInputStream fis = new FileInputStream(QUESTIONS_PATH);
        Workbook wb = WorkbookFactory.create(fis);
        Sheet sheet = wb.getSheet("questions");
        Iterator<Row> rows = sheet.rowIterator();

        while (rows.hasNext()) {
            Row row = rows.next();

            if (row.getCell(1).getStringCellValue().isEmpty()) break; // end at empty cell
            if (row.getRowNum() == 0) continue; // skip header
            if (row.getCell(0).getStringCellValue().equals("t")) continue; // skip trash

            Question question = new Question();
            question.setBody(row.getCell(1).getStringCellValue());
            question.setComment(row.getCell(2).getStringCellValue());
            question.setHot(row.getCell(3).getStringCellValue().equals("h")); // hot question

            questionRepository.save(question);
        }

        fis.close();

        log.info("Preguntas cargadas.");
    }

    public List<QuestionDTO> findAllByHotFalse() {
        List<Question> questions = questionRepository.findAllByHotFalse();

        return mapList(questions);
    }
}
