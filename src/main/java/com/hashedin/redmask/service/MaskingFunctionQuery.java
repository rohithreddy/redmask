package com.hashedin.redmask.service;

public class MaskingFunctionQuery {
  
  public static final String randomIntegerBetween() {
    return "CREATE OR REPLACE FUNCTION " +
        "redmask.random_int_between(int_start integer,\n" + 
        "  int_stop integer\n" + 
        ")\n" + 
        "RETURNS integer AS $$\n" + 
        "BEGIN\n" + 
        "    RETURN (SELECT CAST ( random()*(int_stop-int_start)+int_start AS integer ));\n" + 
        "END\n" + 
        "$$ LANGUAGE plpgsql;";
  }
  
  public static final String randomPhone() {
    return "CREATE OR REPLACE FUNCTION redmask.random_phone(\n" + 
        "  phone_prefix TEXT DEFAULT '0'\n" + 
        ")\n" + 
        "RETURNS TEXT AS $$\n" + 
        "BEGIN\n" + 
        "  RETURN (SELECT  phone_prefix\n" + 
        "          || CAST(redmask.random_int_between(100000000,999999999) AS TEXT)\n" + 
        "          AS \"phone\");\n" + 
        "END\n" + 
        "$$ LANGUAGE plpgsql;";
  }

}
