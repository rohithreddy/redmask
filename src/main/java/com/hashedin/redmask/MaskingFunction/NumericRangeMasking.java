package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.ColumnRule;
import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.service.MaskingFunctionQuery;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Set;

public class NumericRangeMasking extends ColumnRule {
  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingFunctionQuery.maskIntegerRange(config));
    funcSet.add(MaskingFunctionQuery.maskNumericRange(config));
  }

  @Override
  public String getSubQuery(String tableName) {
    return  "redmask.range_numeric(" + this.getName() + ") as " + this.getName();
  }
}
