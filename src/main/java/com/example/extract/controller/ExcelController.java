package com.example.extract.controller;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.extract.dto.SQLCommand;
import com.example.extract.utils.DbInfoUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class ExcelController {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Value("${spring.datasource.driverClassName}")
    private String driver;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String pwd;

    @RequestMapping(value = "/exportplan", method = RequestMethod.GET)
    public void exportPlanExcel(HttpServletResponse response) {
        List<SQLCommand> sqlCommands = jdbcTemplate.query("select table_name as name from all_tables " +
                        "where owner='GHUSR' " +
                        "and (table_name like 'PLAN_QUESTION%' or table_name like 'PLAN_PROJECT%' or table_name like 'PLAN_XZFX%') and  " +
                        "not REGEXP_LIKE(table_name,'*\\d{4}') " +
                        "and table_name not like '%_BAK' and table_name not like '%copy%' " +
                        " and not (upper(table_name) in (select upper(table_name) as name from all_tables\n" +
                        "group by upper(table_name) having count(*)>1) and table_name!=upper(table_name))" +
                        "order by table_name"
                , new RowMapper<SQLCommand>() {
                    @Override
                    public SQLCommand mapRow(ResultSet resultSet, int i) throws SQLException {
                        SQLCommand sqlCommand = new SQLCommand();
                        sqlCommand.setName(resultSet.getString("name"));
                        sqlCommand.setsQLCommand("select * from \"" + sqlCommand.getName() + "\" where rownum<20");
                        return sqlCommand;
                    }
                });
        exportAsSqlCommandList(response, sqlCommands, "plan");
    }

    @RequestMapping(value = "/exportall", method = RequestMethod.GET)
    public void exportAllExcel(HttpServletResponse response) {
        List<SQLCommand> sqlCommands = jdbcTemplate.query("select table_name as name from all_tables " +
                        "where owner='GHUSR' and  not REGEXP_LIKE(table_name,'*\\d{4}') " +
                        "and table_name not like '%_BAK' and table_name not like '%copy%' " +
                        " and not (upper(table_name) in (select upper(table_name) as name from all_tables\n" +
                        "group by upper(table_name) having count(*)>1) and table_name!=upper(table_name))" +
                        "order by table_name"
                , new RowMapper<SQLCommand>() {
                    @Override
                    public SQLCommand mapRow(ResultSet resultSet, int i) throws SQLException {
                        SQLCommand sqlCommand = new SQLCommand();
                        sqlCommand.setName(resultSet.getString("name"));
                        sqlCommand.setsQLCommand("select * from \"" + sqlCommand.getName() + "\" where rownum<20");
                        return sqlCommand;
                    }
                });
        exportAsSqlCommandList(response, sqlCommands, "all");
    }

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
        exportAsSqlCommandList(response, sqlCommands, "plan");
    }

    private void exportAsSqlCommandList(HttpServletResponse response, List<SQLCommand> sqlCommands, String filename) {
        Workbook workbook = new XSSFWorkbook();
        Sheet navSheet = workbook.createSheet("CATEGORY");
        File file = new File("C:/src/typeCodeMap.txt");
        JSONObject typeCodeObject = new JSONObject();
        try {
            typeCodeObject = JSON.parseObject(FileUtils.readFileToString(file, "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final JSONObject typeCodeObjectFinal = typeCodeObject;
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("select type_code||'|::|'||value_code1 as code, value_name as name from sys_codeprop_value");
        Map<String, String> codeMap = new HashMap<>();
        for (Map<String, Object> map : maps) {
            codeMap.put((String) map.get("code"),(String) map.get("name"));
        }

        sqlCommands.stream().filter(sqlCommand -> StringUtils.isNotBlank(sqlCommand.getName())).forEach(sqlCommand -> {
                    String tableComment = "";
                    try {
                        tableComment = jdbcTemplate.queryForObject("select comments from user_tab_comments where table_name = '" + sqlCommand.getName() + "'", String.class);
                    } catch (Exception e) {

                    }
                    final String tableCommentFinal = tableComment;
                    Sheet sheet = workbook.createSheet(sqlCommand.getName());
                    Map<String, String> tableRemarksMap = DbInfoUtil.getTableRemarksMap(driver, url, user, pwd, sqlCommand.getName());
                    try {
                        jdbcTemplate.query(sqlCommand.getsQLCommand(), new ResultSetExtractor<Object>() {
                            @Override
                            public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                                Row row = sheet.createRow(0);
                                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                                    Cell cell = row.createCell(i - 1);
                                    cell.setCellValue(resultSet.getMetaData().getColumnName(i));
                                }
                                row = sheet.createRow(1);
                                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                                    Cell cell = row.createCell(i - 1);
                                    cell.setCellValue(tableRemarksMap.get(resultSet.getMetaData().getColumnName(i)));
                                }
                                int rowNum = 2;

                                while (resultSet.next()) {
                                    row = sheet.createRow(rowNum);
                                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                                        Cell cell = row.createCell(i - 1);
                                        String tableColumnKey = sqlCommand.getName().toUpperCase() + "|::|" + resultSet.getMetaData().getColumnName(i).toUpperCase();
                                        String value = String.valueOf(resultSet.getObject(i));
                                        if (typeCodeObjectFinal.containsKey(tableColumnKey) && StringUtils.isNotBlank(value)) {
                                            String typeCode = (String) typeCodeObjectFinal.get(tableColumnKey);
                                            String valueName = (String) codeMap.get(typeCode + "|::|" + value);
                                            cell.setCellValue(valueName + "[" + value + "]");
                                        } else {
                                            cell.setCellValue(value);
                                        }
                                    }
                                    rowNum++;
                                }
                                row = navSheet.createRow(navSheet.getLastRowNum() + 1);
                                Hyperlink hyperlink = workbook.getCreationHelper().createHyperlink(HyperlinkType.DOCUMENT);
                                hyperlink.setAddress("'" + sqlCommand.getName() + "'!A1");
                                Cell cell = row.createCell(0);
                                CellStyle hlinkstyle = workbook.createCellStyle();
                                Font hlinkfont = workbook.createFont();
                                hlinkfont.setUnderline(XSSFFont.U_SINGLE);
                                hlinkfont.setColor(HSSFColor.BLUE.index);
                                hlinkstyle.setFont(hlinkfont);
                                cell.setHyperlink(hyperlink);
                                cell.setCellStyle(hlinkstyle);
                                cell.setCellValue(sqlCommand.getName());
                                row.createCell(1).setCellValue(rowNum);
                                row.createCell(2).setCellValue(tableCommentFinal);
                                return null;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("error on " + sqlCommand.getName());
                    }
                }
        );

        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".xlsx", "UTF-8"));
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
