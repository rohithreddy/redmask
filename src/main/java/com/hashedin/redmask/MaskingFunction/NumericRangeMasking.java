package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.service.MaskingFunctionQuery;
import com.hashedin.redmask.service.MaskingRuleDef;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class NumericRangeMasking extends MaskingRuleDef {

  public NumericRangeMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  NumericRangeMasking() {}

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingFunctionQuery.maskIntegerRange(config));
    funcSet.add(MaskingFunctionQuery.maskNumericRange(config));
  }

  @Override
  public String getSubQuery(String tableName) {
    return  "redmask.range_numeric(" + this.getColumnName() + ") as " + this.getColumnName();
  }
}
