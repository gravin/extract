package com.example.extract.controller;

import com.example.extract.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        jdbcTemplate.query("select * from all_tables", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                int columnCount = resultSet.getMetaData().getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    resultSet.getObject(i);
                }
            }
        });
        ExcelUtils.exportExcel(personList, "员工信息表", "员工信息", MonitorPersonExportVo.class, "员工信息", response);
    }
}
