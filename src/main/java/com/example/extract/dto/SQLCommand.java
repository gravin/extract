package com.example.extract.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * @author Gavin
 * @date 2020/11/19 21:46
 */
public class SQLCommand {
    @Excel(name="NAME")
    private String name;
    @Excel(name="SQL_COMMAND")
    private String sQLCommand;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getsQLCommand() {
        return sQLCommand;
    }

    public void setsQLCommand(String sQLCommand) {
        this.sQLCommand = sQLCommand;
    }
}
