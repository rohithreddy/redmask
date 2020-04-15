package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.service.MaskingFunctionQuery;
import freemarker.template.TemplateException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class StringMasking extends ColumnRule {
  public StringMasking() {
  }

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet)
      throws IOException, TemplateException {
    funcSet.add(MaskingFunctionQuery.maskString(config));
  }

  @Override
  public String getSubQuery(String tableName) {
    return " redmask.anonymize(" + this.getName() + ") as " + this.getName();
  }
}