package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.service.MaskingFunctionQuery;
import com.hashedin.redmask.service.MaskingRuleDef;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class BigIntRangeMasking extends MaskingRuleDef {

  public BigIntRangeMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public BigIntRangeMasking() {}

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet)
      throws IOException, TemplateException {
    funcSet.add(MaskingFunctionQuery.maskBigIntRange(config));
  }

  @Override
  public String getSubQuery(String tableName) {
    return " redmask.range_int8.range_int4(" + this.getColumnName() + ") as " + this.getColumnName();
  }
}
