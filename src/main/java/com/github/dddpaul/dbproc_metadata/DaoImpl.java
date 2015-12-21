package com.github.dddpaul.dbproc_metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.math.BigDecimal;

@Repository
public class DaoImpl {

    @Autowired
    private DataSource ds;

    private SimpleJdbcCall funcGetNumber99;

    @PostConstruct
    public void init() {
        funcGetNumber99 = new SimpleJdbcCall(ds).withFunctionName("get_number_99");
    }

    public int getNumber99() {
        return funcGetNumber99.executeFunction(BigDecimal.class).intValue();
    }
}
