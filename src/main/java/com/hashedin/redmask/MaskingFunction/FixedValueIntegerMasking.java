package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskConfiguration;
import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class FixedValueIntegerMasking extends MaskingRuleDef {

  public FixedValueIntegerMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  FixedValueIntegerMasking() {}

  @Override
  public void addFunctionDefinition(MaskConfiguration config, Set<String> funcSet) throws IOException, TemplateException {
    funcSet.add(MaskingQueryUtil.maskIntegerFixedValue(config));
  }

  @Override
  public String getSubQuery(String tableName) {
    return "redmask.replaceby(" + this.getColumnName() + ",5) as " + this.getColumnName();
  }
}
