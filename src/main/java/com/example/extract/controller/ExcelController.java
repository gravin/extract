package com.example.extract.controller;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.example.extract.dto.SQLCommand;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/")
public class ExcelController {
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * 导出
     *
     * @param response
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportExcel(HttpServletResponse response) {
        List<SQLCommand> sqlCommands = null;
        try {
            ImportParams params = new ImportParams();
            params.setTitleRows(0);
            params.setHeadRows(1);
            sqlCommands = ExcelImportUtil.importExcel(
                    new ClassPathResource("sql.xlsx").getInputStream(),
                    SQLCommand.class, params);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Workbook workbook = new XSSFWorkbook();
        sqlCommands.stream().forEach(sqlCommand -> {
            Sheet sheet = workbook.createSheet(sqlCommand.getName());
            jdbcTemplate.query(sqlCommand.getsQLCommand(), new RowMapper<Object>() {
                @Override
                public Object mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                    if (rowNum == 0) {
                        Row row = sheet.createRow(0);
                        for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                            Cell cell = row.createCell(i);
                            cell.setCellValue(resultSet.getMetaData().getColumnName(i));
                        }
                        row = sheet.createRow(1);
                        for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                            Cell cell = row.createCell(i);
                            cell.setCellValue(resultSet.getMetaData().getColumnLabel(i));
                        }
                    }
                    Row row = sheet.createRow(rowNum + 2);
                    for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(String.valueOf(resultSet.getObject(i)));
                    }
                    return null;
                }
            });
        });

        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("规划总表.xlsx", "UTF-8"));
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
