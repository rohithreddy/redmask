package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.service.MaskingFunctionQuery;
import com.hashedin.redmask.service.MaskingRuleDef;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class FixedValueFloatMasking extends MaskingRuleDef {

  public FixedValueFloatMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  FixedValueFloatMasking() {}

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingFunctionQuery.maskFloatFixedValue(config));
  }

  @Override
  public String getSubQuery(String tableName) {
    return "redmask.replaceby(" + this.getColumnName() + ",7.0) as " + this.getColumnName();
  }
}
